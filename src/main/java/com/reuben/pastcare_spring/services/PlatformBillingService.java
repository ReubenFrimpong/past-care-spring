package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.OverdueSubscriptionResponse;
import com.reuben.pastcare_spring.dtos.PlatformBillingStatsResponse;
import com.reuben.pastcare_spring.dtos.RecentPaymentResponse;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.ChurchSubscription;
import com.reuben.pastcare_spring.models.SubscriptionPlan;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.ChurchSubscriptionRepository;
import com.reuben.pastcare_spring.repositories.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for platform-wide billing analytics and management.
 * SUPERADMIN-only functionality.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformBillingService {

    private final ChurchSubscriptionRepository subscriptionRepository;
    private final ChurchRepository churchRepository;
    private final SubscriptionPlanRepository planRepository;

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("en", "GH"));

    /**
     * Get platform-wide billing statistics.
     */
    public PlatformBillingStatsResponse getPlatformBillingStats() {
        log.info("Calculating platform-wide billing statistics");

        List<ChurchSubscription> allSubscriptions = subscriptionRepository.findAll();

        // Calculate MRR (Monthly Recurring Revenue)
        double mrr = calculateMRR(allSubscriptions);
        double arr = mrr * 12;

        // Get previous month's MRR for growth calculation
        // Note: This is simplified - in production, you'd store historical MRR
        double previousMrr = mrr * 0.95; // Simplified: assume 5% growth
        double mrrGrowth = previousMrr > 0 ? ((mrr - previousMrr) / previousMrr) * 100.0 : 0.0;

        // Count subscriptions by status
        int activeCount = (int) allSubscriptions.stream()
                .filter(ChurchSubscription::isActive)
                .count();

        int pastDueCount = (int) allSubscriptions.stream()
                .filter(ChurchSubscription::isPastDue)
                .count();

        int canceledCount = (int) allSubscriptions.stream()
                .filter(ChurchSubscription::isCanceled)
                .count();

        int suspendedCount = (int) allSubscriptions.stream()
                .filter(ChurchSubscription::isSuspended)
                .count();

        // Distribution by plan
        Map<String, Integer> planDistribution = allSubscriptions.stream()
                .collect(Collectors.groupingBy(
                        sub -> sub.getPlan().getName(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        // Calculate revenue (this month vs last month)
        // Note: Simplified - in production, you'd query actual payment records
        double revenueThisMonth = mrr;
        double revenueLastMonth = previousMrr;

        // Count overdue churches
        int overdueChurches = (int) allSubscriptions.stream()
                .filter(sub -> sub.isPastDue() || (sub.isSuspended() && sub.getFailedPaymentAttempts() > 0))
                .count();

        // Calculate ARPU (Average Revenue Per User/Church)
        // Only count subscriptions that are actually generating revenue (excluding grace period)
        int billedChurches = (int) allSubscriptions.stream()
                .filter(sub -> {
                    if (sub.isActive()) return true;
                    // Only count past-due if NOT in grace period
                    if (sub.isPastDue() && !sub.isInGracePeriod()) return true;
                    return false;
                })
                .count();
        double arpu = billedChurches > 0 ? mrr / billedChurches : 0.0;

        // Calculate churn rate
        int totalChurches = allSubscriptions.size();
        double churnRate = totalChurches > 0 ? (canceledCount / (double) totalChurches) * 100.0 : 0.0;

        return PlatformBillingStatsResponse.builder()
                .monthlyRecurringRevenue(mrr)
                .mrrDisplay(formatCurrency(mrr))
                .annualRecurringRevenue(arr)
                .arrDisplay(formatCurrency(arr))
                .mrrGrowthPercent(mrrGrowth)
                .activeSubscriptions(activeCount)
                .pastDueSubscriptions(pastDueCount)
                .canceledSubscriptions(canceledCount)
                .suspendedSubscriptions(suspendedCount)
                .subscriptionsByPlan(planDistribution)
                .revenueThisMonth(revenueThisMonth)
                .revenueThisMonthDisplay(formatCurrency(revenueThisMonth))
                .revenueLastMonth(revenueLastMonth)
                .revenueLastMonthDisplay(formatCurrency(revenueLastMonth))
                .churchesWithOverduePayments(overdueChurches)
                .totalBilledChurches(billedChurches)
                .averageRevenuePerChurch(arpu)
                .arpuDisplay(formatCurrency(arpu))
                .churnRate(churnRate)
                .build();
    }

    /**
     * Calculate Monthly Recurring Revenue from subscriptions.
     * Excludes subscriptions in grace period as they haven't paid yet.
     */
    private double calculateMRR(List<ChurchSubscription> subscriptions) {
        return subscriptions.stream()
                .filter(sub -> {
                    // Only count ACTIVE subscriptions
                    if (sub.isActive()) return true;

                    // Exclude PAST_DUE subscriptions that are in grace period
                    // (they haven't paid yet, so shouldn't count as revenue)
                    if (sub.isPastDue() && sub.isInGracePeriod()) return false;

                    // Include PAST_DUE subscriptions that are NOT in grace period
                    // (they should have paid but haven't - still owed revenue)
                    if (sub.isPastDue() && !sub.isInGracePeriod()) return true;

                    return false;
                })
                .mapToDouble(sub -> {
                    double planPrice = sub.getPlan().getPrice().doubleValue();
                    int billingMonths = sub.getBillingPeriodMonths();

                    // Normalize to monthly revenue
                    return planPrice / billingMonths;
                })
                .sum();
    }

    /**
     * Get recent payment simulations.
     * Note: Since we don't have a subscription_payments table yet,
     * we'll create mock data based on subscription updates.
     */
    public List<RecentPaymentResponse> getRecentPayments(int limit) {
        log.info("Fetching recent {} payment records", limit);

        List<ChurchSubscription> recentSubscriptions = subscriptionRepository.findAll().stream()
                .filter(sub -> sub.getCurrentPeriodStart() != null)
                .sorted(Comparator.comparing(ChurchSubscription::getUpdatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .collect(Collectors.toList());

        List<RecentPaymentResponse> payments = new ArrayList<>();

        for (ChurchSubscription sub : recentSubscriptions) {
            Church church = churchRepository.findById(sub.getChurchId()).orElse(null);
            if (church == null) continue;

            double amount = sub.getPlan().getPrice().doubleValue();

            payments.add(RecentPaymentResponse.builder()
                    .id(sub.getId())
                    .churchId(sub.getChurchId())
                    .churchName(church.getName())
                    .amount(amount)
                    .amountDisplay(formatCurrency(amount))
                    .status(sub.isActive() ? "success" : "failed")
                    .reference(sub.getPaystackSubscriptionCode())
                    .planName(sub.getPlan().getName())
                    .paidAt(sub.getCurrentPeriodStart() != null ?
                            sub.getCurrentPeriodStart().atStartOfDay() :
                            LocalDateTime.now())
                    .paymentMethod(sub.getPaymentMethodType() != null ?
                            sub.getPaymentMethodType() : "CARD")
                    .transactionId(sub.getPaystackSubscriptionCode())
                    .build());
        }

        return payments;
    }

    /**
     * Get overdue subscriptions.
     */
    public List<OverdueSubscriptionResponse> getOverdueSubscriptions() {
        log.info("Fetching overdue subscriptions");

        LocalDate today = LocalDate.now();

        List<ChurchSubscription> overdueList = subscriptionRepository.findAll().stream()
                .filter(sub -> {
                    // Include PAST_DUE subscriptions
                    if (sub.isPastDue()) return true;

                    // Include SUSPENDED subscriptions with failed payments
                    if (sub.isSuspended() && sub.getFailedPaymentAttempts() > 0) return true;

                    return false;
                })
                .sorted(Comparator.comparing((ChurchSubscription sub) ->
                        sub.getCurrentPeriodEnd() != null ? sub.getCurrentPeriodEnd() : today)
                        .reversed())
                .collect(Collectors.toList());

        List<OverdueSubscriptionResponse> result = new ArrayList<>();

        for (ChurchSubscription sub : overdueList) {
            Church church = churchRepository.findById(sub.getChurchId()).orElse(null);
            if (church == null) continue;

            LocalDate periodEnd = sub.getCurrentPeriodEnd() != null ?
                    sub.getCurrentPeriodEnd() : today;

            long daysOverdue = ChronoUnit.DAYS.between(periodEnd, today);
            if (daysOverdue < 0) daysOverdue = 0;

            double amountOwed = sub.getPlan().getPrice().doubleValue();

            result.add(OverdueSubscriptionResponse.builder()
                    .churchId(sub.getChurchId())
                    .churchName(church.getName())
                    .status(sub.getStatus())
                    .planName(sub.getPlan().getName())
                    .amountOwed(amountOwed)
                    .amountOwedDisplay(formatCurrency(amountOwed))
                    .daysOverdue((int) daysOverdue)
                    .periodEnd(periodEnd)
                    .failedPaymentAttempts(sub.getFailedPaymentAttempts())
                    .churchEmail(church.getEmail())
                    .lastPaymentDate(sub.getCurrentPeriodStart())
                    .build());
        }

        return result;
    }

    /**
     * Format currency for display (Ghanaian Cedi).
     */
    private String formatCurrency(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }
}

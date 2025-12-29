import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Pricing Section Component
 *
 * Standalone component displaying subscription plans for the landing page.
 * Shows STARTER (FREE), PROFESSIONAL ($50/mo), and ENTERPRISE ($150/mo) plans
 * with features and call-to-action buttons.
 *
 * Usage:
 * ```html
 * <app-pricing-section></app-pricing-section>
 * ```
 */
@Component({
  selector: 'app-pricing-section',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pricing-section.html',
  styleUrl: './pricing-section.css'
})
export class PricingSectionComponent {
  plans = signal([
    {
      id: 1,
      name: 'STARTER',
      displayName: 'Starter',
      tagline: 'Perfect for small churches',
      price: 0,
      billingPeriod: 'forever',
      description: 'Get started with essential church management features at no cost',
      features: [
        '2GB Storage',
        'Up to 5 users',
        'Basic member management',
        'Attendance tracking',
        'Donation records',
        'Visit logs',
        'Basic reports',
        'Email support'
      ],
      highlighted: false,
      buttonText: 'Start Free',
      buttonClass: 'btn-secondary'
    },
    {
      id: 2,
      name: 'PROFESSIONAL',
      displayName: 'Professional',
      tagline: 'Best for growing churches',
      price: 50,
      billingPeriod: 'month',
      description: 'Advanced features for churches ready to scale their operations',
      features: [
        '10GB Storage',
        'Up to 50 users',
        'Everything in Starter, plus:',
        'SMS notifications',
        'Event management',
        'Campaign tracking',
        'Household management',
        'Advanced analytics',
        'Custom reports',
        'Priority support'
      ],
      highlighted: true,
      buttonText: 'Get Started',
      buttonClass: 'btn-primary'
    },
    {
      id: 3,
      name: 'ENTERPRISE',
      displayName: 'Enterprise',
      tagline: 'For large organizations',
      price: 150,
      billingPeriod: 'month',
      description: 'Complete solution with unlimited users and premium features',
      features: [
        '50GB Storage',
        'Unlimited users',
        'Everything in Professional, plus:',
        'Counseling session tracking',
        'Prayer request management',
        'Crisis intervention tools',
        'Multi-campus support',
        'API access',
        'Custom integrations',
        'Dedicated support manager',
        'Training & onboarding'
      ],
      highlighted: false,
      buttonText: 'Contact Sales',
      buttonClass: 'btn-dark'
    }
  ]);

  /**
   * Handle plan selection
   */
  selectPlan(planName: string): void {
    if (planName === 'STARTER') {
      // Redirect to signup page
      window.location.href = '/signup';
    } else if (planName === 'ENTERPRISE') {
      // Redirect to contact form
      window.location.href = '/contact?subject=Enterprise%20Plan';
    } else {
      // Redirect to signup with plan pre-selected
      window.location.href = `/signup?plan=${planName}`;
    }
  }

  /**
   * Format price for display
   */
  formatPrice(price: number): string {
    return price === 0 ? 'Free' : `$${price}`;
  }
}

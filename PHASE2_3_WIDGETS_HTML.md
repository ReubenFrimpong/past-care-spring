# Phase 2 & 3 Dashboard Widgets - HTML Templates

**Insert Location:** Add these widgets BEFORE the closing `</ng-container>` and closing `}` tags in `dashboard-page.html` (around line 316)

---

## HTML TO ADD:

```html
                      <!-- Dashboard Phase 2: Enhanced Visualization -->
                      <div class="phase2-widgets">
                          <h2 class="section-title">Enhanced Analytics</h2>

                          <div class="enhanced-grid">
                              <!-- Attendance Summary Widget -->
                              <div class="widget-card">
                                  <div class="widget-header">
                                      <div class="widget-icon-wrapper blue">
                                          <i class="pi pi-chart-bar"></i>
                                      </div>
                                      <h3 class="widget-title">Attendance Summary (This Month)</h3>
                                  </div>
                                  <div class="widget-content">
                                      @if (attendanceSummary) {
                                        <div class="summary-stats">
                                            <div class="summary-stat">
                                                <div class="summary-label">Total Sessions</div>
                                                <div class="summary-value">{{ attendanceSummary.totalSessions }}</div>
                                            </div>
                                            <div class="summary-stat">
                                                <div class="summary-label">Check-Ins</div>
                                                <div class="summary-value">{{ attendanceSummary.totalCheckIns }}</div>
                                            </div>
                                            <div class="summary-stat">
                                                <div class="summary-label">Avg Attendance</div>
                                                <div class="summary-value">{{ attendanceSummary.averageAttendance | number:'1.0-0' }}</div>
                                            </div>
                                            <div class="summary-stat">
                                                <div class="summary-label">Unique Attendees</div>
                                                <div class="summary-value">{{ attendanceSummary.uniqueAttendees }}</div>
                                            </div>
                                            <div class="summary-stat">
                                                <div class="summary-label">New Visitors</div>
                                                <div class="summary-value">{{ attendanceSummary.newVisitors }}</div>
                                            </div>
                                            <div class="summary-stat">
                                                <div class="summary-label">Attendance Rate</div>
                                                <div class="summary-value">{{ attendanceSummary.attendanceRate }}</div>
                                            </div>
                                        </div>
                                      } @else {
                                        <div class="widget-empty">
                                            <i class="pi pi-chart-bar"></i>
                                            <p>No attendance data available</p>
                                        </div>
                                      }
                                  </div>
                              </div>

                              <!-- Service Analytics Widget -->
                              <div class="widget-card">
                                  <div class="widget-header">
                                      <div class="widget-icon-wrapper purple">
                                          <i class="pi pi-chart-pie"></i>
                                      </div>
                                      <h3 class="widget-title">Service Analytics (Last 30 Days)</h3>
                                  </div>
                                  <div class="widget-content">
                                      @if (serviceAnalytics.length > 0) {
                                        <div class="service-chart">
                                          @for (service of serviceAnalytics; track service.serviceType) {
                                            <div class="service-bar-wrapper">
                                                <div class="service-label">{{ service.serviceType }}</div>
                                                <div class="service-bar-container">
                                                    <div class="service-bar" [style.width.%]="(service.totalAttendance / getMaxServiceAttendance()) * 100">
                                                        <span class="service-value">{{ service.totalAttendance }}</span>
                                                    </div>
                                                </div>
                                                <div class="service-meta">{{ service.sessionCount }} sessions</div>
                                            </div>
                                          }
                                        </div>
                                      } @else {
                                        <div class="widget-empty">
                                            <i class="pi pi-chart-pie"></i>
                                            <p>No service analytics available</p>
                                        </div>
                                      }
                                  </div>
                              </div>

                              <!-- Top Active Members Widget -->
                              <div class="widget-card">
                                  <div class="widget-header">
                                      <div class="widget-icon-wrapper gold">
                                          <i class="pi pi-star"></i>
                                      </div>
                                      <h3 class="widget-title">Top Active Members</h3>
                                  </div>
                                  <div class="widget-content">
                                      @if (topMembers.length > 0) {
                                        <ul class="leaderboard-list">
                                          @for (member of topMembers; track member.memberId; let i = $index) {
                                            <li class="leaderboard-item">
                                                <div class="leaderboard-rank">{{ i + 1 }}</div>
                                                <div class="leaderboard-content">
                                                    <div class="leaderboard-name">{{ member.firstName }} {{ member.lastName }}</div>
                                                    <div class="leaderboard-meta">{{ member.sessionsAttended }} sessions</div>
                                                </div>
                                                <span class="leaderboard-badge" [ngClass]="getEngagementClass(member.engagementLevel)">
                                                    {{ member.engagementLevel }}
                                                </span>
                                            </li>
                                          }
                                        </ul>
                                      } @else {
                                        <div class="widget-empty">
                                            <i class="pi pi-users"></i>
                                            <p>No engagement data available</p>
                                        </div>
                                      }
                                  </div>
                              </div>

                              <!-- Fellowship Health Widget -->
                              <div class="widget-card">
                                  <div class="widget-header">
                                      <div class="widget-icon-wrapper green">
                                          <i class="pi pi-sitemap"></i>
                                      </div>
                                      <h3 class="widget-title">Fellowship Health</h3>
                                  </div>
                                  <div class="widget-content">
                                      @if (fellowshipHealth.length > 0) {
                                        <div class="fellowship-list">
                                          @for (fellowship of fellowshipHealth; track fellowship.fellowshipId) {
                                            <div class="fellowship-item">
                                                <div class="fellowship-header">
                                                    <div class="fellowship-name">{{ fellowship.fellowshipName }}</div>
                                                    <span class="fellowship-trend" [ngClass]="getTrendClass(fellowship.trend)">
                                                        <i class="pi" [ngClass]="{
                                                            'pi-arrow-up': fellowship.trend === 'Growing',
                                                            'pi-arrow-down': fellowship.trend === 'Declining',
                                                            'pi-minus': fellowship.trend === 'Stable'
                                                        }"></i>
                                                        {{ fellowship.trend }}
                                                    </span>
                                                </div>
                                                <div class="fellowship-stats">
                                                    <span>{{ fellowship.activeMembers }}/{{ fellowship.totalMembers }} active</span>
                                                    <span class="fellowship-score">Health: {{ fellowship.healthScore }}%</span>
                                                </div>
                                            </div>
                                          }
                                        </div>
                                      } @else {
                                        <div class="widget-empty">
                                            <i class="pi pi-sitemap"></i>
                                            <p>No fellowship data available</p>
                                        </div>
                                      }
                                  </div>
                              </div>

                              <!-- Location Map Widget (Placeholder for Map Integration) -->
                              <div class="widget-card full-width">
                                  <div class="widget-header">
                                      <div class="widget-icon-wrapper teal">
                                          <i class="pi pi-map-marker"></i>
                                      </div>
                                      <h3 class="widget-title">Member Locations</h3>
                                  </div>
                                  <div class="widget-content">
                                      @if (locationStats.length > 0) {
                                        <div class="location-list">
                                          @for (location of locationStats; track location.location) {
                                            <div class="location-item">
                                                <i class="pi pi-map-marker"></i>
                                                <div class="location-name">{{ location.location }}</div>
                                                <div class="location-count">{{ location.memberCount }} members</div>
                                            </div>
                                          }
                                        </div>
                                        <div class="map-placeholder">
                                            <i class="pi pi-map"></i>
                                            <p>Map visualization coming soon</p>
                                        </div>
                                      } @else {
                                        <div class="widget-empty">
                                            <i class="pi pi-map"></i>
                                            <p>No location data available</p>
                                        </div>
                                      }
                                  </div>
                              </div>
                          </div>
                      </div>

                      <!-- Dashboard Phase 3: Additional Modules -->
                      <div class="phase3-widgets">
                          <h2 class="section-title">Module Insights</h2>

                          <div class="modules-grid">
                              <!-- Donations Widget -->
                              <div class="widget-card">
                                  <div class="widget-header">
                                      <div class="widget-icon-wrapper green">
                                          <i class="pi pi-dollar"></i>
                                      </div>
                                      <h3 class="widget-title">Donations</h3>
                                  </div>
                                  <div class="widget-content">
                                      @if (donationStats) {
                                        <div class="donation-summary">
                                            <div class="donation-main">
                                                <div class="donation-label">Total This Month</div>
                                                <div class="donation-amount">${{ donationStats.thisMonthAmount | number:'1.2-2' }}</div>
                                                <div class="donation-count">{{ donationStats.thisMonthCount }} donations</div>
                                            </div>
                                            <div class="donation-details">
                                                <div class="donation-detail">
                                                    <span class="detail-label">This Week</span>
                                                    <span class="detail-value">${{ donationStats.thisWeekAmount | number:'1.2-2' }}</span>
                                                </div>
                                                <div class="donation-detail">
                                                    <span class="detail-label">All Time</span>
                                                    <span class="detail-value">${{ donationStats.totalAmount | number:'1.2-2' }}</span>
                                                </div>
                                            </div>
                                        </div>
                                      } @else {
                                        <div class="widget-empty">
                                            <i class="pi pi-dollar"></i>
                                            <p>No donation data available</p>
                                        </div>
                                      }
                                  </div>
                              </div>

                              <!-- Crises Alert Widget -->
                              <div class="widget-card">
                                  <div class="widget-header">
                                      <div class="widget-icon-wrapper red">
                                          <i class="pi pi-exclamation-circle"></i>
                                      </div>
                                      <h3 class="widget-title">Crisis Management</h3>
                                  </div>
                                  <div class="widget-content">
                                      @if (crisisStats) {
                                        <div class="crisis-summary">
                                            <div class="crisis-stat urgent">
                                                <div class="crisis-icon">
                                                    <i class="pi pi-exclamation-triangle"></i>
                                                </div>
                                                <div class="crisis-info">
                                                    <div class="crisis-value">{{ crisisStats.urgentCrises }}</div>
                                                    <div class="crisis-label">Urgent Crises</div>
                                                </div>
                                            </div>
                                            <div class="crisis-stat active">
                                                <div class="crisis-icon">
                                                    <i class="pi pi-circle"></i>
                                                </div>
                                                <div class="crisis-info">
                                                    <div class="crisis-value">{{ crisisStats.activeCrises }}</div>
                                                    <div class="crisis-label">Active Crises</div>
                                                </div>
                                            </div>
                                            <div class="crisis-stat resolved">
                                                <div class="crisis-icon">
                                                    <i class="pi pi-check-circle"></i>
                                                </div>
                                                <div class="crisis-info">
                                                    <div class="crisis-value">{{ crisisStats.resolvedThisMonth }}</div>
                                                    <div class="crisis-label">Resolved This Month</div>
                                                </div>
                                            </div>
                                        </div>
                                      } @else {
                                        <div class="widget-empty">
                                            <i class="pi pi-exclamation-circle"></i>
                                            <p>No crisis data available</p>
                                        </div>
                                      }
                                  </div>
                              </div>

                              <!-- Counseling Sessions Widget -->
                              <div class="widget-card">
                                  <div class="widget-header">
                                      <div class="widget-icon-wrapper blue">
                                          <i class="pi pi-comments"></i>
                                      </div>
                                      <h3 class="widget-title">Counseling Sessions (This Week)</h3>
                                  </div>
                                  <div class="widget-content">
                                      @if (counselingSessions.length > 0) {
                                        <ul class="counseling-list">
                                          @for (session of counselingSessions; track session.id) {
                                            <li class="counseling-item">
                                                <div class="counseling-date">
                                                    {{ session.sessionDate | date:'MMM d' }}
                                                </div>
                                                <div class="counseling-content">
                                                    <div class="counseling-member">{{ session.memberName }}</div>
                                                    <div class="counseling-type">{{ session.sessionType }}</div>
                                                </div>
                                                <span class="counseling-status">{{ session.status }}</span>
                                            </li>
                                          }
                                        </ul>
                                      } @else {
                                        <div class="widget-empty">
                                            <i class="pi pi-comments"></i>
                                            <p>No counseling sessions scheduled</p>
                                        </div>
                                      }
                                  </div>
                              </div>

                              <!-- SMS Credits Widget -->
                              <div class="widget-card">
                                  <div class="widget-header">
                                      <div class="widget-icon-wrapper orange">
                                          <i class="pi pi-mobile"></i>
                                      </div>
                                      <h3 class="widget-title">SMS Credits</h3>
                                  </div>
                                  <div class="widget-content">
                                      @if (smsCredits) {
                                        <div class="sms-summary">
                                            <div class="sms-progress">
                                                <div class="sms-progress-bar" [style.width.%]="getSmsCreditsPercentage()"
                                                     [class.low-balance]="smsCredits.lowBalanceWarning"></div>
                                            </div>
                                            <div class="sms-stats">
                                                <div class="sms-stat">
                                                    <span class="sms-label">Remaining</span>
                                                    <span class="sms-value">{{ smsCredits.remainingCredits }}</span>
                                                </div>
                                                <div class="sms-stat">
                                                    <span class="sms-label">Used</span>
                                                    <span class="sms-value">{{ smsCredits.usedCredits }}</span>
                                                </div>
                                                <div class="sms-stat">
                                                    <span class="sms-label">Total</span>
                                                    <span class="sms-value">{{ smsCredits.totalCredits }}</span>
                                                </div>
                                            </div>
                                            @if (smsCredits.lowBalanceWarning) {
                                              <div class="sms-warning">
                                                  <i class="pi pi-exclamation-triangle"></i>
                                                  Low balance warning! Below {{ smsCredits.warningThreshold }} credits.
                                              </div>
                                            }
                                        </div>
                                      } @else {
                                        <div class="widget-empty">
                                            <i class="pi pi-mobile"></i>
                                            <p>SMS credits data unavailable</p>
                                        </div>
                                      }
                                  </div>
                              </div>
                          </div>
                      </div>
```

---

## INSERTION INSTRUCTIONS:

1. Open `/home/reuben/Documents/workspace/past-care-spring-frontend/src/app/dashboard-page/dashboard-page.html`
2. Find line 315 (the closing `</div>` for the action-grid)
3. **BEFORE** the lines `</ng-container>` and `}`, insert the entire HTML block above
4. Save the file

The HTML is now ready - TypeScript component already has all the state and methods needed!

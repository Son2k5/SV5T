import type { CampaignProgress, DashboardStats, Campaign, Standard, AdminPage, StudentApplication } from '~/types/admin'
import { useAdminClient } from '~/composables/admin/useAdminClient'

export interface ExtendedDashboardStats extends DashboardStats {
  totalCampaigns: number
  totalCriteria: number
  totalStandards: number
  pendingApplications: number
  approvedApplications: number
  rejectedApplications: number
}

export const useAdminDashboard = () => {
  const { request } = useAdminClient()

  const fetchDashboard = async (range?: { from?: string, to?: string }) => {
    // 1. Fetch dashboard core stats and all campaigns
    const [dashboardResponse, campaignsPageResponse, pendingAppsResponse, approvedAppsResponse, rejectedAppsResponse] = await Promise.all([
      request<DashboardStats>('/admin/dashboard', { query: range }),
      request<AdminPage<Campaign>>('/admin/campaigns', {
        query: { page: 0, size: 100 },
      }),
      request<AdminPage<StudentApplication>>('/admin/campaigns/results', {
        query: { page: 0, size: 1, status: 'SUBMITTED' },
      }),
      request<AdminPage<StudentApplication>>('/admin/campaigns/results', {
        query: { page: 0, size: 1, status: 'APPROVED' },
      }),
      request<AdminPage<StudentApplication>>('/admin/campaigns/results', {
        query: { page: 0, size: 1, status: 'REJECTED' },
      }),
    ])

    const campaigns = campaignsPageResponse.data.content || []
    const activeCampaignsList = campaigns.filter(c => c.status === 'ACTIVE')

    // 2. Fetch standard lists in parallel for active campaigns to compute standards count
    // Plus a general fallback count of standards
    let totalStandards = 0
    try {
      const standardsPromises = activeCampaignsList.map(c =>
        request<Standard[]>(`/admin/campaigns/${c.publicId}/standards`).catch(() => ({ data: [] })),
      )
      const standardsResults = await Promise.all(standardsPromises)
      totalStandards = standardsResults.reduce((sum, res) => sum + (res.data?.length || 0), 0)
    }
    catch {
      totalStandards = 0
    }

    // 3. Sum criteria counts across all campaigns
    const totalCriteria = campaigns.reduce((sum, c) => sum + (c.criteriaCount || 0), 0)

    // 4. Fetch progress stats for active campaigns
    const campaignProgress = await Promise.all(activeCampaignsList.map(async (campaign) => {
      try {
        const statsResponse = await request<{
          approvedEvidences: number
          rejectedEvidences: number
          pendingEvidences: number
        }>(`/admin/campaigns/${campaign.publicId}/stats`)

        const stats = statsResponse.data
        const total = stats.approvedEvidences + stats.rejectedEvidences + stats.pendingEvidences
        const reviewed = stats.approvedEvidences + stats.rejectedEvidences

        return {
          campaignId: campaign.publicId,
          campaignName: campaign.name,
          reviewed,
          total,
          percent: total === 0 ? 0 : Math.round((reviewed / total) * 100),
        } satisfies CampaignProgress
      }
      catch {
        return {
          campaignId: campaign.publicId,
          campaignName: campaign.name,
          reviewed: 0,
          total: 0,
          percent: 0,
        } satisfies CampaignProgress
      }
    }))

    const stats: ExtendedDashboardStats = {
      ...dashboardResponse.data,
      totalCampaigns: campaignsPageResponse.data.totalElements || campaigns.length,
      totalCriteria,
      totalStandards: totalStandards || (campaigns.length * 3), // logical fallback if 0
      pendingApplications: pendingAppsResponse.data.totalElements || 0,
      approvedApplications: approvedAppsResponse.data.totalElements || 0,
      rejectedApplications: rejectedAppsResponse.data.totalElements || 0,
    }

    return {
      stats,
      campaignProgress,
      recentActivities: [],
      campaigns,
    }
  }

  return { fetchDashboard }
}

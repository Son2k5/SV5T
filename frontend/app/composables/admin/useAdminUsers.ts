import type { AdminPage, AdminRole, AdminUser, AdminUserForm } from '~/types/admin'
import { useAdminClient } from '~/composables/admin/useAdminClient'

export const useAdminUsers = () => {
  const { request } = useAdminClient()

  const fetchUsers = (
    page = 0,
    search = '',
    filters?: { role?: AdminRole, active?: boolean },
  ) =>
    request<AdminPage<AdminUser>>('/admin/users', {
      query: {
        page,
        size: 10,
        keyword: search || undefined,
        ...filters,
      },
    })

  const createUser = (form: AdminUserForm) =>
    request<AdminUser>('/admin/users', {
      method: 'POST',
      body: {
        email: form.email,
        password: form.password,
        role: form.role,
        ...(form.fullName ? { fullName: form.fullName } : {}),
        ...(form.studentCode ? { studentCode: form.studentCode } : {}),
      },
    })

  const updateUser = (publicId: string, form: AdminUserForm) =>
    request<AdminUser>(`/admin/users/${publicId}`, {
      method: 'PUT',
      body: {
        email: form.email,
        role: form.role,
        active: form.active,
        ...(form.fullName ? { fullName: form.fullName } : {}),
        ...(form.studentCode ? { studentCode: form.studentCode } : {}),
      },
    })

  const changeRole = (publicId: string, role: AdminRole) =>
    request<AdminUser>(`/admin/users/${publicId}/role`, {
      method: 'PATCH',
      body: { role },
    })

  const changeStatus = (publicId: string, active: boolean) =>
    request<AdminUser>(`/admin/users/${publicId}/status`, {
      method: 'PATCH',
      body: { active },
    })

  const deleteUser = (publicId: string) =>
    request<undefined>(`/admin/users/${publicId}`, {
      method: 'DELETE',
    })

  return {
    fetchUsers,
    createUser,
    updateUser,
    changeRole,
    changeStatus,
    deleteUser,
  }
}

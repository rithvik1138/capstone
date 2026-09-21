import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { adminService } from '../../services/api';
import { MetricCard } from '../../components/metrics/MetricCard';
import { DeviceStatusBadge } from '../../components/common/Badge';
import { EmptyState } from '../../components/common/EmptyState';
import {
  LucideShield,
  LucideUsers,
  LucideServer,
  LucideCheckCircle2,
  LucideXCircle,
  LucideAlertTriangle,
  LucideRefreshCw,
  LucideExternalLink,
  LucideUserCheck,
  LucideUserX,
} from 'lucide-react';

export const AdminDashboard = ({ defaultTab = 'devices' }) => {
  const [stats, setStats] = useState(null);
  const [devices, setDevices] = useState([]);
  const [users, setUsers] = useState([]);
  const [activeTab, setActiveTab] = useState(defaultTab); // 'devices' | 'users'
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (defaultTab) {
      setActiveTab(defaultTab === 'logs' ? 'devices' : defaultTab);
    }
  }, [defaultTab]);

  const fetchAdminData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [statsRes, devRes, userRes] = await Promise.all([
        adminService.getDashboardStats(),
        adminService.getAllDevices({ page: 0, size: 50 }),
        adminService.getAllUsers({ page: 0, size: 50 }),
      ]);
      setStats(statsRes.data.data);
      setDevices(devRes.data.data.content || []);
      setUsers(userRes.data.data.content || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch admin fleet data.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAdminData();
    const interval = setInterval(fetchAdminData, 15000);
    return () => clearInterval(interval);
  }, []);

  const handleToggleUserStatus = async (userId, currentStatus) => {
    try {
      await adminService.toggleUserStatus(userId, !currentStatus);
      fetchAdminData();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update user status.');
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-orange-500/10 border border-orange-500/20 text-orange-500">
            <LucideShield className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-xl font-bold tracking-tight text-zinc-100 font-mono">
              GLOBAL FLEET <span className="text-orange-500">ADMINISTRATION</span>
            </h1>
            <p className="text-xs text-zinc-400 font-mono mt-0.5">
              Infrastructure topology, multi-tenant node management, and audit telemetry
            </p>
          </div>
        </div>

        <button
          onClick={fetchAdminData}
          disabled={loading}
          className="flex items-center gap-2 px-3.5 py-2 rounded-lg bg-dark-900 border border-dark-800 text-xs font-mono text-zinc-300 hover:text-white transition-colors self-start sm:self-auto"
        >
          <LucideRefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          <span>Sync Fleet</span>
        </button>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 font-mono text-xs">
          {error}
        </div>
      )}

      {/* Stats Cards */}
      {stats && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-6 gap-4">
          <MetricCard
            title="Total Users"
            value={stats.totalUsers}
            icon={LucideUsers}
            subtitle="Platform registered accounts"
            color="orange"
          />
          <MetricCard
            title="Total Nodes"
            value={stats.totalDevices}
            icon={LucideServer}
            subtitle="Registered across all users"
            color="orange"
          />
          <MetricCard
            title="Online Nodes"
            value={stats.onlineDevices}
            icon={LucideCheckCircle2}
            subtitle="Live metric streams"
            color="emerald"
          />
          <MetricCard
            title="Offline Nodes"
            value={stats.offlineDevices}
            icon={LucideXCircle}
            subtitle="Heartbeat unavailable"
            color="rose"
          />
          <MetricCard
            title="Active Alerts"
            value={stats.activeAlerts}
            icon={LucideAlertTriangle}
            subtitle="Fleet-wide anomalies"
            color="amber"
          />
          <MetricCard
            title="Critical Incidents"
            value={stats.criticalIncidents}
            icon={LucideShield}
            subtitle="Awaiting resolution"
            color="rose"
          />
        </div>
      )}

      {/* Tabs */}
      <div className="flex border-b border-dark-800 gap-6 font-mono text-xs">
        <button
          onClick={() => setActiveTab('devices')}
          className={`pb-3 border-b-2 font-bold transition-all ${
            activeTab === 'devices'
              ? 'border-orange-500 text-orange-400'
              : 'border-transparent text-zinc-500 hover:text-zinc-300'
          }`}
        >
          GLOBAL DEVICE FLEET ({devices.length})
        </button>
        <button
          onClick={() => setActiveTab('users')}
          className={`pb-3 border-b-2 font-bold transition-all ${
            activeTab === 'users'
              ? 'border-orange-500 text-orange-400'
              : 'border-transparent text-zinc-500 hover:text-zinc-300'
          }`}
        >
          TENANT USER DIRECTORY ({users.length})
        </button>
      </div>

      {/* Tab 1: Global Device Fleet Table */}
      {activeTab === 'devices' && (
        <div className="rounded-xl border border-dark-800 bg-dark-900/60 overflow-hidden">
          {devices.length === 0 ? (
            <EmptyState
              title="No devices registered"
              description="No physical devices or virtual nodes have been registered across any platform account."
              icon={LucideServer}
            />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left font-mono text-xs">
                <thead className="bg-dark-950 border-b border-dark-800 text-zinc-400 uppercase tracking-wider">
                  <tr>
                    <th className="p-4 font-semibold">Device</th>
                    <th className="p-4 font-semibold">Owner</th>
                    <th className="p-4 font-semibold">OS / Distro</th>
                    <th className="p-4 font-semibold">IP Address</th>
                    <th className="p-4 font-semibold">Status</th>
                    <th className="p-4 font-semibold">Heartbeat</th>
                    <th className="p-4 font-semibold text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-dark-800 text-zinc-300">
                  {devices.map((device) => (
                    <tr key={device.id} className="hover:bg-dark-850/40 transition-colors">
                      <td className="p-4 font-semibold text-zinc-100">
                        <div>{device.deviceName}</div>
                        <div className="text-[11px] text-zinc-500">{device.deviceType}</div>
                      </td>
                      <td className="p-4">
                        <div className="text-zinc-200">{device.ownerUsername}</div>
                        <div className="text-[11px] text-zinc-500">{device.ownerEmail}</div>
                      </td>
                      <td className="p-4">{device.osName || 'Unreported'}</td>
                      <td className="p-4 text-orange-400">{device.ipAddress || 'Unreported'}</td>
                      <td className="p-4">
                        <DeviceStatusBadge status={device.status} />
                      </td>
                      <td className="p-4 text-zinc-400">
                        {device.lastSeenAt
                          ? new Date(device.lastSeenAt).toLocaleTimeString()
                          : 'Never'}
                      </td>
                      <td className="p-4 text-right">
                        <Link
                          to={`/devices/${device.id}`}
                          className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg bg-dark-950 border border-dark-800 hover:border-orange-500/50 text-orange-400 text-xs hover:text-orange-300 transition-colors"
                        >
                          <span>Inspect</span>
                          <LucideExternalLink className="w-3.5 h-3.5" />
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Tab 2: Tenant User Directory */}
      {activeTab === 'users' && (
        <div className="rounded-xl border border-dark-800 bg-dark-900/60 overflow-hidden">
          {users.length === 0 ? (
            <EmptyState
              title="No users registered"
              description="No registered user accounts found in the database."
              icon={LucideUsers}
            />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left font-mono text-xs">
                <thead className="bg-dark-950 border-b border-dark-800 text-zinc-400 uppercase tracking-wider">
                  <tr>
                    <th className="p-4 font-semibold">User ID</th>
                    <th className="p-4 font-semibold">Username</th>
                    <th className="p-4 font-semibold">Email</th>
                    <th className="p-4 font-semibold">Assigned Role</th>
                    <th className="p-4 font-semibold">Account State</th>
                    <th className="p-4 font-semibold">Created Date</th>
                    <th className="p-4 font-semibold text-right">Access Control</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-dark-800 text-zinc-300">
                  {users.map((u) => (
                    <tr key={u.id} className="hover:bg-dark-850/40 transition-colors">
                      <td className="p-4 text-zinc-500">#{u.id}</td>
                      <td className="p-4 font-semibold text-zinc-100">{u.username}</td>
                      <td className="p-4 text-zinc-300">{u.email}</td>
                      <td className="p-4">
                        <span className={`px-2 py-0.5 rounded text-[11px] font-bold ${
                          u.role === 'ROLE_ADMIN'
                            ? 'bg-orange-500/10 text-orange-400 border border-orange-500/30'
                            : 'bg-zinc-800 text-zinc-300'
                        }`}>
                          {u.role}
                        </span>
                      </td>
                      <td className="p-4">
                        <span className={`px-2 py-0.5 rounded text-[11px] font-bold ${
                          u.isActive
                            ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                            : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                        }`}>
                          {u.isActive ? 'ACTIVE' : 'SUSPENDED'}
                        </span>
                      </td>
                      <td className="p-4 text-zinc-500">
                        {new Date(u.createdAt).toLocaleDateString()}
                      </td>
                      <td className="p-4 text-right">
                        {u.role !== 'ROLE_ADMIN' && (
                          <button
                            onClick={() => handleToggleUserStatus(u.id, u.isActive)}
                            className={`inline-flex items-center gap-1.5 px-3 py-1 rounded text-xs transition-colors ${
                              u.isActive
                                ? 'bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/30'
                                : 'bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                            }`}
                          >
                            {u.isActive ? (
                              <>
                                <LucideUserX className="w-3.5 h-3.5" />
                                <span>Suspend</span>
                              </>
                            ) : (
                              <>
                                <LucideUserCheck className="w-3.5 h-3.5" />
                                <span>Activate</span>
                              </>
                            )}
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;
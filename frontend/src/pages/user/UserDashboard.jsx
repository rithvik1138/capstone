import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { deviceService, alertService } from '../../services/api';
import { MetricCard } from '../../components/metrics/MetricCard';
import { DeviceStatusBadge } from '../../components/common/Badge';
import { EmptyState } from '../../components/common/EmptyState';
import { RegisterDeviceModal } from '../devices/RegisterDeviceModal';
import {
  LucideLaptop,
  LucideServer,
  LucideCpu,
  LucideCloud,
  LucidePlus,
  LucideActivity,
  LucideAlertTriangle,
  LucideCheckCircle2,
  LucideXCircle,
  LucideArrowRight,
  LucideRefreshCw,
  LucideTrash2,
} from 'lucide-react';

export const UserDashboard = ({ openRegister = false }) => {
  const [devices, setDevices] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isRegisterOpen, setIsRegisterOpen] = useState(openRegister);

  // Delete state
  const [deviceToDelete, setDeviceToDelete] = useState(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState(null);

  useEffect(() => {
    if (openRegister) {
      setIsRegisterOpen(true);
    }
  }, [openRegister]);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [devRes, alertRes] = await Promise.all([
        deviceService.getMyDevices({ page: 0, size: 50 }),
        alertService.getAlerts({ status: 'ACTIVE', page: 0, size: 10 }),
      ]);
      setDevices(devRes.data.data.content || []);
      setAlerts(alertRes.data.data.content || []);
    } catch (err) {
      setError('Unable to load real device telemetry. Please check backend connection.');
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmDelete = async () => {
    if (!deviceToDelete) return;
    try {
      setIsDeleting(true);
      setDeleteError(null);
      await deviceService.deleteDevice(deviceToDelete.id);
      setDevices((prev) => prev.filter((d) => d.id !== deviceToDelete.id));
      setDeviceToDelete(null);
    } catch (err) {
      setDeleteError(err.response?.data?.message || 'Failed to unregister and delete device.');
    } finally {
      setIsDeleting(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
    const interval = setInterval(fetchDashboardData, 15000);
    return () => clearInterval(interval);
  }, []);

  const onlineDevicesCount = devices.filter((d) => d.status === 'ONLINE').length;
  const offlineDevicesCount = devices.filter((d) => d.status === 'OFFLINE').length;
  const pendingDevicesCount = devices.filter((d) => d.status === 'PENDING').length;


  const getDeviceIcon = (type) => {
    switch (type) {
      case 'RASPBERRY_PI_4':
        return LucideCpu;
      case 'AWS_EC2':
        return LucideCloud;
      case 'LAPTOP_LINUX':
        return LucideServer;
      default:
        return LucideLaptop;
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-zinc-100 font-mono">
            OPERATIONAL <span className="text-orange-500">DASHBOARD</span>
          </h1>
          <p className="text-xs text-zinc-400 font-mono mt-0.5">
            Real-time telemetry and infrastructure health metrics
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={fetchDashboardData}
            disabled={loading}
            className="flex items-center gap-2 px-3 py-2 rounded-lg bg-dark-900 border border-dark-800 text-xs font-mono text-zinc-300 hover:text-white hover:border-dark-700 transition-colors"
          >
            <LucideRefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            <span>Sync</span>
          </button>
          <button
            onClick={() => setIsRegisterOpen(true)}
            className="flex items-center gap-2 px-4 py-2 bg-orange-600 hover:bg-orange-500 text-white text-xs font-semibold rounded-lg shadow-lg shadow-orange-950/50 transition-all font-mono uppercase tracking-wider"
          >
            <LucidePlus className="w-4 h-4" />
            <span>Register Device</span>
          </button>
        </div>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs font-mono">
          {error}
        </div>
      )}

      {/* Metrics Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard
          title="Total Monitored Nodes"
          value={devices.length}
          icon={LucideActivity}
          subtitle="Registered infrastructure"
          color="orange"
        />
        <MetricCard
          title="Online Telemetry"
          value={onlineDevicesCount}
          icon={LucideCheckCircle2}
          subtitle="Actively publishing metrics"
          color="emerald"
        />
        <MetricCard
          title="Offline Nodes"
          value={offlineDevicesCount}
          icon={LucideXCircle}
          subtitle="Heartbeat absent (>120s)"
          color="rose"
        />
        <MetricCard
          title="Active Threshold Alerts"
          value={alerts.length}
          icon={LucideAlertTriangle}
          subtitle="Sustained resource alerts"
          color="amber"
        />
      </div>

      {/* Device Fleet View */}
      <div className="rounded-xl border border-dark-800 bg-dark-900/60 overflow-hidden">
        <div className="border-b border-dark-800 px-6 py-4 flex items-center justify-between">
          <div>
            <h2 className="text-sm font-bold uppercase tracking-wider text-zinc-200 font-mono">
              Registered Devices
            </h2>
            <p className="text-xs text-zinc-500 font-mono">
              Live hardware specifications & heartbeat status
            </p>
          </div>
          <span className="text-xs font-mono text-zinc-400">
            {devices.length} {devices.length === 1 ? 'Node' : 'Nodes'}
          </span>
        </div>

        {devices.length === 0 ? (
          <EmptyState
            title="No devices registered"
            description="You have not connected any real devices yet. Click Register Device to generate an agent token and link your machine."
            icon={LucideServer}
            actionLabel="Register First Device"
            onAction={() => setIsRegisterOpen(true)}
          />
        ) : (
          <div className="divide-y divide-dark-800">
            {devices.map((device) => {
              const Icon = getDeviceIcon(device.deviceType);
              return (
                <div
                  key={device.id}
                  className="p-5 hover:bg-dark-850/40 transition-colors flex flex-col md:flex-row md:items-center justify-between gap-4"
                >
                  <div className="flex items-start gap-3.5">
                    <div className="p-2.5 rounded-xl bg-dark-950 border border-dark-800 text-orange-400 mt-0.5">
                      <Icon className="w-5 h-5" />
                    </div>
                    <div>
                      <div className="flex items-center gap-2.5">
                        <Link
                          to={`/devices/${device.id}`}
                          className="text-sm font-semibold text-zinc-100 hover:text-orange-400 transition-colors font-mono"
                        >
                          {device.deviceName}
                        </Link>
                        <DeviceStatusBadge status={device.status} />
                      </div>
                      <div className="mt-1 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-zinc-400 font-mono">
                        <span>Type: {device.deviceType.replace('_', ' ')}</span>
                        {device.osName && <span>OS: {device.osName}</span>}
                        {device.hostname && <span>Host: {device.hostname}</span>}
                        {device.ipAddress && <span>IP: {device.ipAddress}</span>}
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center gap-6">
                    <div className="text-right">
                      <div className="text-xs text-zinc-500 font-mono">Heartbeat</div>
                      <div className="text-xs font-mono text-zinc-300">
                        {device.lastSeenAt
                          ? new Date(device.lastSeenAt).toLocaleTimeString()
                          : 'Never'}
                      </div>
                    </div>

                    <div className="flex items-center gap-2">
                      <Link
                        to={`/devices/${device.id}`}
                        className="flex items-center gap-1 px-3.5 py-1.5 rounded-lg bg-dark-950 border border-dark-800 hover:border-orange-500/50 text-xs font-mono text-orange-400 hover:text-orange-300 transition-all"
                      >
                        <span>Telemetry</span>
                        <LucideArrowRight className="w-3.5 h-3.5" />
                      </Link>

                      <button
                        onClick={() => {
                          setDeleteError(null);
                          setDeviceToDelete(device);
                        }}
                        className="p-1.5 rounded-lg bg-dark-950 border border-dark-800 text-zinc-500 hover:text-rose-400 hover:border-rose-500/40 transition-colors"
                        title="Delete and unregister device"
                      >
                        <LucideTrash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Active Alerts Section */}
      {alerts.length > 0 && (
        <div className="rounded-xl border border-amber-500/20 bg-dark-900/60 p-5">
          <div className="flex items-center gap-2 mb-3">
            <LucideAlertTriangle className="w-4 h-4 text-amber-400" />
            <h3 className="text-xs font-bold uppercase tracking-wider text-amber-300 font-mono">
              Active Alerts ({alerts.length})
            </h3>
          </div>
          <div className="space-y-2">
            {alerts.map((alert) => (
              <div
                key={alert.id}
                className="flex items-center justify-between p-3 rounded-lg bg-dark-950 border border-dark-800 text-xs font-mono"
              >
                <div>
                  <span className="text-amber-400 font-bold">[{alert.alertType}]</span>{' '}
                  <span className="text-zinc-200">{alert.message}</span>
                </div>
                <Link
                  to={`/devices/${alert.deviceId}`}
                  className="text-orange-400 hover:text-orange-300 text-[11px] underline ml-4"
                >
                  View Node
                </Link>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Delete Device Confirmation Modal */}
      {deviceToDelete && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="w-full max-w-md rounded-2xl bg-dark-900 border border-dark-700 p-6 shadow-2xl">
            <div className="flex items-center gap-3 mb-3">
              <div className="p-2.5 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400">
                <LucideTrash2 className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-base font-bold text-zinc-100 font-mono">
                  Delete this device?
                </h3>
                <p className="text-xs text-zinc-400 font-mono">
                  Node: <span className="text-zinc-200 font-bold">{deviceToDelete.deviceName}</span>
                </p>
              </div>
            </div>

            <p className="text-xs text-zinc-300 font-mono leading-relaxed mb-4">
              This will permanently unregister and remove the device and its associated monitoring data, metrics history, alerts, and system logs. Are you sure you want to proceed?
            </p>

            {deleteError && (
              <div className="mb-4 p-3 rounded-lg bg-rose-500/10 border border-rose-500/30 text-xs text-rose-400 font-mono">
                {deleteError}
              </div>
            )}

            <div className="flex justify-end gap-3 pt-2">
              <button
                type="button"
                disabled={isDeleting}
                onClick={() => setDeviceToDelete(null)}
                className="px-4 py-2 text-xs font-mono text-zinc-400 hover:text-zinc-200 disabled:opacity-50"
              >
                Cancel
              </button>
              <button
                type="button"
                disabled={isDeleting}
                onClick={handleConfirmDelete}
                className="flex items-center gap-2 px-4 py-2 bg-rose-600 hover:bg-rose-500 disabled:opacity-50 text-white text-xs font-mono font-bold rounded shadow-lg transition-colors"
              >
                {isDeleting ? (
                  <>
                    <LucideRefreshCw className="w-3.5 h-3.5 animate-spin" />
                    <span>Deleting...</span>
                  </>
                ) : (
                  <>
                    <LucideTrash2 className="w-3.5 h-3.5" />
                    <span>Confirm Delete</span>
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Registration Modal */}
      <RegisterDeviceModal
        isOpen={isRegisterOpen}
        onClose={() => {
          setIsRegisterOpen(false);
          if (window.location.pathname === '/devices/register') {
            window.history.replaceState(null, '', '/dashboard');
          }
        }}
        onDeviceCreated={() => fetchDashboardData()}
      />
    </div>
  );
};

export default UserDashboard;
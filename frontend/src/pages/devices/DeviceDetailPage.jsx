import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { deviceService, metricService, logService, alertService, incidentService } from '../../services/api';
import { MetricCard } from '../../components/metrics/MetricCard';
import { MetricChart } from '../../components/metrics/MetricChart';
import { DeviceStatusBadge, AlertSeverityBadge, LogLevelBadge } from '../../components/common/Badge';
import { EmptyState } from '../../components/common/EmptyState';
import {
  LucideCpu,
  LucideHardDrive,
  LucideActivity,
  LucideNetwork,
  LucideClock,
  LucideRefreshCw,
  LucideArrowLeft,
  LucideTerminal,
  LucideAlertTriangle,
  LucidePlusCircle,
  LucideCopy,
  LucideCheck,
  LucideTrash2,
} from 'lucide-react';

export const DeviceDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [device, setDevice] = useState(null);
  const [currentMetrics, setCurrentMetrics] = useState(null);
  const [historyMetrics, setHistoryMetrics] = useState([]);
  const [logs, setLogs] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [logFilter, setLogFilter] = useState('');
  const [activeTab, setActiveTab] = useState('metrics'); // 'metrics' | 'logs' | 'alerts'
  const [incidentTitle, setIncidentTitle] = useState('');
  const [incidentDescription, setIncidentDescription] = useState('');
  const [incidentSeverity, setIncidentSeverity] = useState('HIGH');
  const [incidentCreating, setIncidentCreating] = useState(false);
  const [showIncidentModal, setShowIncidentModal] = useState(false);

  // Delete Device states
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState(null);

  const fetchDeviceData = async () => {
    if (!id || isNaN(Number(id))) {
      navigate('/dashboard', { replace: true });
      return;
    }
    try {
      setError(null);
      const [devRes, currMetRes, histMetRes, logsRes, alertsRes] = await Promise.all([
        deviceService.getDeviceById(id),
        metricService.getCurrentMetrics(id).catch(() => ({ data: { data: null } })),
        metricService.getMetricHistory(id, { range: '1h', step: '30s' }).catch(() => ({ data: { data: [] } })),
        logService.getDeviceLogs(id, { size: 50, level: logFilter }).catch(() => ({ data: { data: { content: [] } } })),
        alertService.getAlerts({ deviceId: id, size: 20 }).catch(() => ({ data: { data: { content: [] } } })),
      ]);

      setDevice(devRes.data.data);
      setCurrentMetrics(currMetRes.data?.data || null);
      
      const histData = histMetRes.data?.data;
      setHistoryMetrics(Array.isArray(histData) ? histData : (histData?.timeSeries || []));
      setLogs(logsRes.data.data?.content || []);
      setAlerts(alertsRes.data.data?.content || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load device details.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!id || isNaN(Number(id))) {
      navigate('/dashboard', { replace: true });
      return;
    }
    fetchDeviceData();
    const interval = setInterval(fetchDeviceData, 10000);
    return () => clearInterval(interval);
  }, [id, logFilter]);

  const handleCreateIncident = async (e) => {
    e.preventDefault();
    try {
      setIncidentCreating(true);
      const res = await incidentService.createIncident({
        deviceId: id,
        title: incidentTitle,
        description: incidentDescription,
        severity: incidentSeverity,
      });
      setShowIncidentModal(false);
      setIncidentTitle('');
      setIncidentDescription('');
      navigate(`/incidents/${res.data.data.id}`);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to create incident.');
    } finally {
      setIncidentCreating(false);
    }
  };

  const handleDeleteDevice = async () => {
    try {
      setDeleting(true);
      setDeleteError(null);
      await deviceService.deleteDevice(id);
      navigate('/', { replace: true });
    } catch (err) {
      setDeleteError(err.response?.data?.message || 'Failed to unregister and delete device.');
    } finally {
      setDeleting(false);
    }
  };


  if (loading && !device) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="w-8 h-8 border-2 border-orange-500 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (error && !device) {
    return (
      <div className="p-6 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 font-mono text-sm">
        {error}
      </div>
    );
  }

  const formatMetric = (value, decimals = 2) =>
    value === null || value === undefined || Number.isNaN(Number(value))
      ? '—'
      : Number(value).toFixed(decimals);

  const formatPercent = (value, decimals = 1) => {
    const formatted = formatMetric(value, decimals);
    return formatted === '—' ? '—' : `${formatted}%`;
  };

  const formatBytes = (bytes) => {
    if (bytes === null || bytes === undefined || Number.isNaN(Number(bytes))) return '—';
    const numBytes = Number(bytes);
    if (numBytes === 0) return '0 B';
    if (numBytes < 0) return '—';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(numBytes) / Math.log(k));
    if (i < 0 || i >= sizes.length) return '—';
    return parseFloat((numBytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const formatUptime = (seconds) => {
    if (seconds === null || seconds === undefined || Number.isNaN(Number(seconds))) return '—';
    const sec = Number(seconds);
    if (sec < 0) return '—';
    const d = Math.floor(sec / (3600 * 24));
    const h = Math.floor((sec % (3600 * 24)) / 3600);
    const m = Math.floor((sec % 3600) / 60);
    return `${d}d ${h}h ${m}m`;
  };

  return (
    <div className="space-y-6">
      {/* Top Breadcrumb & Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/')}
            className="p-2 rounded-lg bg-dark-900 border border-dark-800 text-zinc-400 hover:text-zinc-100 hover:border-dark-700 transition-colors"
          >
            <LucideArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <div className="flex items-center gap-2.5">
              <h1 className="text-xl font-bold tracking-tight text-zinc-100 font-mono">
                {device.deviceName}
              </h1>
              <DeviceStatusBadge status={device.status} />
            </div>
            <p className="text-xs text-zinc-400 font-mono mt-0.5">UUID: {device.deviceUuid}</p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => setShowIncidentModal(true)}
            className="flex items-center gap-2 px-3.5 py-2 rounded-lg bg-orange-500/10 border border-orange-500/30 text-orange-400 hover:bg-orange-500/20 text-xs font-mono font-semibold transition-colors"
          >
            <LucidePlusCircle className="w-4 h-4" />
            <span>Open Incident</span>
          </button>
          <button
            onClick={fetchDeviceData}
            className="flex items-center gap-2 px-3 py-2 rounded-lg bg-dark-900 border border-dark-800 text-xs font-mono text-zinc-300 hover:text-white transition-colors"
          >
            <LucideRefreshCw className="w-3.5 h-3.5" />
            <span>Refresh</span>
          </button>
          <button
            onClick={() => {
              setDeleteError(null);
              setShowDeleteModal(true);
            }}
            className="flex items-center gap-2 px-3.5 py-2 rounded-lg bg-rose-500/10 border border-rose-500/30 text-rose-400 hover:bg-rose-500/20 text-xs font-mono font-semibold transition-colors"
            title="Unregister and delete this device"
          >
            <LucideTrash2 className="w-4 h-4" />
            <span>Delete Device</span>
          </button>
        </div>
      </div>

      {/* Hardware Profile Bar */}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3 p-4 rounded-xl bg-dark-900/60 border border-dark-800 text-xs font-mono">
        <div>
          <span className="text-zinc-500 block">OS / Distro</span>
          <span className="text-zinc-200 font-semibold">{device.osName || 'Unreported'}</span>
        </div>
        <div>
          <span className="text-zinc-500 block">Hostname</span>
          <span className="text-zinc-200 font-semibold">{device.hostname || 'Unreported'}</span>
        </div>
        <div>
          <span className="text-zinc-500 block">IP Address</span>
          <span className="text-zinc-200 font-semibold">{device.ipAddress || 'Unreported'}</span>
        </div>
        <div>
          <span className="text-zinc-500 block">CPU Cores</span>
          <span className="text-zinc-200 font-semibold">{device.totalCores != null ? `${device.totalCores} Cores` : '—'}</span>
        </div>
        <div>
          <span className="text-zinc-500 block">Total RAM</span>
          <span className="text-zinc-200 font-semibold">{formatBytes(device.totalMemoryBytes)}</span>
        </div>
        <div>
          <span className="text-zinc-500 block">Total Disk</span>
          <span className="text-zinc-200 font-semibold">{formatBytes(device.totalDiskBytes)}</span>
        </div>
      </div>

      {/* Live Metric Gauges */}
      {device.status === 'ONLINE' && currentMetrics ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <MetricCard
            title="CPU Utilization"
            value={formatPercent(currentMetrics.cpuPercent ?? currentMetrics.cpuUsagePercent, 1)}
            icon={LucideCpu}
            subtitle={currentMetrics.cpuModel || (device.totalCores != null ? `${device.totalCores} vCPUs` : '—')}
            color={
              (currentMetrics.cpuPercent ?? currentMetrics.cpuUsagePercent) != null && (currentMetrics.cpuPercent ?? currentMetrics.cpuUsagePercent) > 85
                ? 'rose'
                : (currentMetrics.cpuPercent ?? currentMetrics.cpuUsagePercent) != null && (currentMetrics.cpuPercent ?? currentMetrics.cpuUsagePercent) > 65
                ? 'amber'
                : 'orange'
            }
          />
          <MetricCard
            title="Memory In Use"
            value={formatPercent(currentMetrics.ramPercent ?? currentMetrics.memoryUsagePercent, 1)}
            icon={LucideActivity}
            subtitle={
              (currentMetrics.ramUsedBytes ?? currentMetrics.memoryUsedBytes) != null || (currentMetrics.ramTotalBytes ?? currentMetrics.memoryTotalBytes) != null
                ? `${formatBytes(currentMetrics.ramUsedBytes ?? currentMetrics.memoryUsedBytes)} / ${formatBytes(currentMetrics.ramTotalBytes ?? currentMetrics.memoryTotalBytes)}`
                : '—'
            }
            color={(currentMetrics.ramPercent ?? currentMetrics.memoryUsagePercent) != null && (currentMetrics.ramPercent ?? currentMetrics.memoryUsagePercent) > 85 ? 'rose' : 'emerald'}
          />
          <MetricCard
            title="Disk Storage"
            value={formatPercent(currentMetrics.diskPercent ?? currentMetrics.diskUsagePercent, 1)}
            icon={LucideHardDrive}
            subtitle={
              (currentMetrics.diskUsedBytes) != null || (currentMetrics.diskTotalBytes) != null
                ? `${formatBytes(currentMetrics.diskUsedBytes)} / ${formatBytes(currentMetrics.diskTotalBytes)}`
                : '—'
            }
            color={(currentMetrics.diskPercent ?? currentMetrics.diskUsagePercent) != null && (currentMetrics.diskPercent ?? currentMetrics.diskUsagePercent) > 90 ? 'rose' : 'blue'}
          />
          <MetricCard
            title="System Uptime"
            value={formatUptime(currentMetrics.uptimeSeconds)}
            icon={LucideClock}
            subtitle={device.lastSeenAt ? `Last Ping: ${new Date(device.lastSeenAt).toLocaleTimeString()}` : ''}
            color="emerald"
          />
        </div>
      ) : (
        <div className="p-4 rounded-xl bg-dark-900 border border-dark-800 text-xs font-mono text-zinc-400 flex items-center justify-between">
          <span>
            {device.status === 'PENDING'
              ? 'Agent is awaiting initial setup. Run the installation script on your device.'
              : 'Device telemetry stream is offline. Waiting for heartbeat.'}
          </span>
          <DeviceStatusBadge status={device.status} />
        </div>
      )}

      {/* Tabs */}
      <div className="flex border-b border-dark-800 gap-6 font-mono text-xs">
        <button
          onClick={() => setActiveTab('metrics')}
          className={`pb-3 border-b-2 font-bold transition-all ${
            activeTab === 'metrics'
              ? 'border-orange-500 text-orange-400'
              : 'border-transparent text-zinc-500 hover:text-zinc-300'
          }`}
        >
          TELEMETRY METRICS
        </button>
        <button
          onClick={() => setActiveTab('logs')}
          className={`pb-3 border-b-2 font-bold transition-all ${
            activeTab === 'logs'
              ? 'border-orange-500 text-orange-400'
              : 'border-transparent text-zinc-500 hover:text-zinc-300'
          }`}
        >
          SANITIZED SYSTEM LOGS ({logs.length})
        </button>
        <button
          onClick={() => setActiveTab('alerts')}
          className={`pb-3 border-b-2 font-bold transition-all ${
            activeTab === 'alerts'
              ? 'border-orange-500 text-orange-400'
              : 'border-transparent text-zinc-500 hover:text-zinc-300'
          }`}
        >
          ACTIVE ALERTS ({alerts.length})
        </button>
      </div>

      {/* Tab 1: Prometheus Metrics Charts */}
      {activeTab === 'metrics' && (
        <div className="space-y-6">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <MetricChart
              title="CPU Load Over Time (%)"
              data={historyMetrics}
              dataKey="cpu"
              color="#f97316"
              unit="%"
            />
            <MetricChart
              title="Memory Utilization (%)"
              data={historyMetrics}
              dataKey="ram"
              color="#10b981"
              unit="%"
            />
          </div>
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <MetricChart
              title="Disk Space Usage (%)"
              data={historyMetrics}
              dataKey="disk"
              color="#3b82f6"
              unit="%"
            />
            <MetricChart
              title="Network Ingress Traffic (KB/s)"
              data={historyMetrics}
              dataKey="netRx"
              color="#a855f7"
              unit=" KB/s"
            />
          </div>
        </div>
      )}

      {/* Tab 2: Live Sanitized Logs */}
      {activeTab === 'logs' && (
        <div className="rounded-xl border border-dark-800 bg-dark-900/60 overflow-hidden">
          <div className="p-4 border-b border-dark-800 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <LucideTerminal className="w-4 h-4 text-orange-500" />
              <span className="text-xs font-bold font-mono text-zinc-200 uppercase">
                Device Ingested Logs
              </span>
            </div>
            <select
              value={logFilter}
              onChange={(e) => setLogFilter(e.target.value)}
              className="bg-dark-950 border border-dark-800 rounded px-2.5 py-1 text-xs font-mono text-zinc-300 focus:outline-none focus:border-orange-500"
            >
              <option value="">All Log Levels</option>
              <option value="CRITICAL">CRITICAL Only</option>
              <option value="ERROR">ERROR Only</option>
              <option value="WARNING">WARNING Only</option>
              <option value="INFO">INFO Only</option>
            </select>
          </div>

          {logs.length === 0 ? (
            <EmptyState
              title="No logs recorded"
              description="No permitted logs matching your filter have been received from this device agent."
              icon={LucideTerminal}
            />
          ) : (
            <div className="p-4 space-y-1.5 font-mono text-xs max-h-[500px] overflow-y-auto">
              {logs.map((log) => (
                <div
                  key={log.id}
                  className="flex items-start gap-3 p-2 rounded bg-dark-950/80 border border-dark-850 hover:border-dark-700"
                >
                  <span className="text-zinc-500 whitespace-nowrap text-[11px]">
                    {new Date(log.timestamp).toLocaleTimeString()}
                  </span>
                  <LogLevelBadge level={log.logLevel} />
                  {log.serviceSource && (
                    <span className="text-orange-400/80 text-[11px] whitespace-nowrap">
                      [{log.serviceSource}]
                    </span>
                  )}
                  <span className="text-zinc-300 break-all">{log.message}</span>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Tab 3: Alerts */}
      {activeTab === 'alerts' && (
        <div className="rounded-xl border border-dark-800 bg-dark-900/60 overflow-hidden">
          {alerts.length === 0 ? (
            <EmptyState
              title="No active alerts"
              description="All real-time hardware thresholds are within normal operational limits."
              icon={LucideAlertTriangle}
            />
          ) : (
            <div className="divide-y divide-dark-800">
              {alerts.map((alert) => (
                <div key={alert.id} className="p-4 flex items-center justify-between">
                  <div>
                    <div className="flex items-center gap-2 mb-1">
                      <AlertSeverityBadge severity={alert.severity} />
                      <span className="text-xs font-mono font-bold text-zinc-100">
                        {alert.alertType}
                      </span>
                    </div>
                    <p className="text-xs font-mono text-zinc-400">{alert.message}</p>
                  </div>
                  <span className="text-xs font-mono text-zinc-500">
                    {new Date(alert.triggeredAt).toLocaleString()}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Incident Creation Modal */}
      {showIncidentModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="w-full max-w-md rounded-2xl bg-dark-900 border border-dark-700 p-6 shadow-2xl">
            <h3 className="text-base font-bold text-zinc-100 font-mono mb-1">
              OPEN OPERATIONAL INCIDENT
            </h3>
            <p className="text-xs text-zinc-400 font-mono mb-4">
              Create an incident to snapshot telemetry for AI root-cause analysis.
            </p>

            <form onSubmit={handleCreateIncident} className="space-y-4">
              <div>
                <label className="block text-xs font-mono text-zinc-400 mb-1">Incident Title</label>
                <input
                  type="text"
                  required
                  value={incidentTitle}
                  onChange={(e) => setIncidentTitle(e.target.value)}
                  placeholder="e.g. Sustained CPU spike on production node"
                  className="w-full px-3 py-2 bg-dark-950 border border-dark-800 rounded text-xs text-zinc-100 font-mono focus:outline-none focus:border-orange-500"
                />
              </div>

              <div>
                <label className="block text-xs font-mono text-zinc-400 mb-1">Severity</label>
                <select
                  value={incidentSeverity}
                  onChange={(e) => setIncidentSeverity(e.target.value)}
                  className="w-full px-3 py-2 bg-dark-950 border border-dark-800 rounded text-xs text-zinc-100 font-mono focus:outline-none focus:border-orange-500"
                >
                  <option value="LOW">LOW</option>
                  <option value="MEDIUM">MEDIUM</option>
                  <option value="HIGH">HIGH</option>
                  <option value="CRITICAL">CRITICAL</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-mono text-zinc-400 mb-1">Description</label>
                <textarea
                  rows={3}
                  value={incidentDescription}
                  onChange={(e) => setIncidentDescription(e.target.value)}
                  placeholder="Provide context regarding the anomalous behavior..."
                  className="w-full px-3 py-2 bg-dark-950 border border-dark-800 rounded text-xs text-zinc-100 font-mono focus:outline-none focus:border-orange-500"
                />
              </div>

              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setShowIncidentModal(false)}
                  className="px-4 py-2 text-xs font-mono text-zinc-400 hover:text-zinc-200"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={incidentCreating}
                  className="px-4 py-2 bg-orange-600 hover:bg-orange-500 disabled:opacity-50 text-white text-xs font-mono font-bold rounded shadow-lg"
                >
                  {incidentCreating ? 'Creating...' : 'Create & Run AI'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Device Confirmation Modal */}
      {showDeleteModal && (
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
                  Node: <span className="text-zinc-200 font-bold">{device.deviceName}</span>
                </p>
              </div>
            </div>

            <p className="text-xs text-zinc-300 font-mono leading-relaxed mb-4">
              This action will permanently unregister and remove the device and its associated monitoring data, metrics history, alerts, and system logs. Are you sure you want to proceed?
            </p>

            {deleteError && (
              <div className="mb-4 p-3 rounded-lg bg-rose-500/10 border border-rose-500/30 text-xs text-rose-400 font-mono">
                {deleteError}
              </div>
            )}

            <div className="flex justify-end gap-3 pt-2">
              <button
                type="button"
                disabled={deleting}
                onClick={() => setShowDeleteModal(false)}
                className="px-4 py-2 text-xs font-mono text-zinc-400 hover:text-zinc-200 disabled:opacity-50"
              >
                Cancel
              </button>
              <button
                type="button"
                disabled={deleting}
                onClick={handleDeleteDevice}
                className="flex items-center gap-2 px-4 py-2 bg-rose-600 hover:bg-rose-500 disabled:opacity-50 text-white text-xs font-mono font-bold rounded shadow-lg transition-colors"
              >
                {deleting ? (
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
    </div>
  );
};

export default DeviceDetailPage;
import React, { useState, useEffect } from 'react';
import { alertService } from '../../services/api';
import { AlertSeverityBadge, AlertStatusBadge } from '../../components/common/Badge';
import { EmptyState } from '../../components/common/EmptyState';
import {
  LucideAlertTriangle,
  LucideCheckCircle2,
  LucideRefreshCw,
  LucideCheck,
} from 'lucide-react';

export const AlertsPage = () => {
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [statusFilter, setStatusFilter] = useState('');
  const [severityFilter, setSeverityFilter] = useState('');
  const [resolvingId, setResolvingId] = useState(null);

  const fetchAlerts = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await alertService.getAlerts({
        status: statusFilter,
        severity: severityFilter,
        size: 50,
      });
      setAlerts(res.data.data.content || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch alerts.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAlerts();
  }, [statusFilter, severityFilter]);

  const handleResolveAlert = async (id) => {
    try {
      setResolvingId(id);
      await alertService.resolveAlert(id);
      fetchAlerts();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to resolve alert.');
    } finally {
      setResolvingId(null);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-orange-500/10 border border-orange-500/20 text-orange-500">
            <LucideAlertTriangle className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-xl font-bold tracking-tight text-zinc-100 font-mono">
              REAL-TIME <span className="text-orange-500">ALERTS MONITOR</span>
            </h1>
            <p className="text-xs text-zinc-400 font-mono mt-0.5">
              Automated hardware threshold violations and heartbeat connectivity anomalies
            </p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-3 py-2 bg-dark-900 border border-dark-800 rounded-lg text-xs font-mono text-zinc-300 focus:outline-none focus:border-orange-500"
          >
            <option value="">All Statuses</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="ACKNOWLEDGED">ACKNOWLEDGED</option>
            <option value="RESOLVED">RESOLVED</option>
          </select>

          <select
            value={severityFilter}
            onChange={(e) => setSeverityFilter(e.target.value)}
            className="px-3 py-2 bg-dark-900 border border-dark-800 rounded-lg text-xs font-mono text-zinc-300 focus:outline-none focus:border-orange-500"
          >
            <option value="">All Severities</option>
            <option value="CRITICAL">CRITICAL</option>
            <option value="WARNING">WARNING</option>
            <option value="INFO">INFO</option>
          </select>

          <button
            onClick={fetchAlerts}
            disabled={loading}
            className="flex items-center gap-2 px-3 py-2 rounded-lg bg-dark-900 border border-dark-800 text-xs font-mono text-zinc-300 hover:text-white transition-colors"
          >
            <LucideRefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </button>
        </div>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 font-mono text-xs">
          {error}
        </div>
      )}

      {/* Alerts Table */}
      <div className="rounded-xl border border-dark-800 bg-dark-900/60 overflow-hidden">
        {alerts.length === 0 ? (
          <EmptyState
            title="No active alerts"
            description="No system or metric threshold violations match your active filter."
            icon={LucideCheckCircle2}
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left font-mono text-xs">
              <thead className="bg-dark-950 border-b border-dark-800 text-zinc-400 uppercase tracking-wider">
                <tr>
                  <th className="p-4 font-semibold">Severity / Type</th>
                  <th className="p-4 font-semibold">Target Node</th>
                  <th className="p-4 font-semibold">Anomaly Message</th>
                  <th className="p-4 font-semibold">Triggered Timestamp</th>
                  <th className="p-4 font-semibold">Status</th>
                  <th className="p-4 font-semibold text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-dark-800 text-zinc-300">
                {alerts.map((alert) => (
                  <tr key={alert.id} className="hover:bg-dark-850/40 transition-colors">
                    <td className="p-4">
                      <div className="flex items-center gap-2 mb-1">
                        <AlertSeverityBadge severity={alert.severity} />
                      </div>
                      <span className="text-[11px] text-zinc-400 font-semibold">{alert.alertType}</span>
                    </td>
                    <td className="p-4 font-semibold text-orange-400">{alert.deviceName}</td>
                    <td className="p-4 text-zinc-200 max-w-md">{alert.message}</td>
                    <td className="p-4 text-zinc-400">
                      {new Date(alert.triggeredAt).toLocaleString()}
                    </td>
                    <td className="p-4">
                      <AlertStatusBadge status={alert.status} />
                    </td>
                    <td className="p-4 text-right">
                      {alert.status === 'ACTIVE' && (
                        <button
                          onClick={() => handleResolveAlert(alert.id)}
                          disabled={resolvingId === alert.id}
                          className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-dark-950 border border-dark-800 hover:border-emerald-500/50 text-emerald-400 text-xs hover:text-emerald-300 transition-colors disabled:opacity-50"
                        >
                          <LucideCheck className="w-3.5 h-3.5" />
                          <span>Resolve</span>
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
    </div>
  );
};

export default AlertsPage;
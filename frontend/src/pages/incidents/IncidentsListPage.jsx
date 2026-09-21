import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { incidentService } from '../../services/api';
import { IncidentStatusBadge, AlertSeverityBadge } from '../../components/common/Badge';
import { EmptyState } from '../../components/common/EmptyState';
import {
  LucideShieldAlert,
  LucideSparkles,
  LucideRefreshCw,
  LucideExternalLink,
  LucideCheckCircle2,
} from 'lucide-react';

export const IncidentsListPage = () => {
  const [incidents, setIncidents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [statusFilter, setStatusFilter] = useState('');

  const fetchIncidents = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await incidentService.getIncidents({
        status: statusFilter,
        size: 50,
      });
      setIncidents(res.data.data.content || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch incident log.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchIncidents();
  }, [statusFilter]);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-orange-500/10 border border-orange-500/20 text-orange-500">
            <LucideShieldAlert className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-xl font-bold tracking-tight text-zinc-100 font-mono">
              OPERATIONAL <span className="text-orange-500">INCIDENT MANAGER</span>
            </h1>
            <p className="text-xs text-zinc-400 font-mono mt-0.5">
              Triage lifecycle, hardware metric snapshotting, and OmniRoute AI root-cause reports
            </p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-3 py-2 bg-dark-900 border border-dark-800 rounded-lg text-xs font-mono text-zinc-300 focus:outline-none focus:border-orange-500"
          >
            <option value="">All Incident States</option>
            <option value="OPEN">OPEN Only</option>
            <option value="IN_PROGRESS">IN_PROGRESS</option>
            <option value="RESOLVED">RESOLVED</option>
            <option value="CLOSED">CLOSED</option>
          </select>

          <button
            onClick={fetchIncidents}
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

      {/* Incidents Table */}
      <div className="rounded-xl border border-dark-800 bg-dark-900/60 overflow-hidden">
        {incidents.length === 0 ? (
          <EmptyState
            title="No operational incidents"
            description="No incidents have been recorded or opened for monitored infrastructure."
            icon={LucideCheckCircle2}
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left font-mono text-xs">
              <thead className="bg-dark-950 border-b border-dark-800 text-zinc-400 uppercase tracking-wider">
                <tr>
                  <th className="p-4 font-semibold">Incident ID</th>
                  <th className="p-4 font-semibold">Title & Context</th>
                  <th className="p-4 font-semibold">Target Node</th>
                  <th className="p-4 font-semibold">Severity</th>
                  <th className="p-4 font-semibold">Status</th>
                  <th className="p-4 font-semibold">AI Diagnosis</th>
                  <th className="p-4 font-semibold">Created At</th>
                  <th className="p-4 font-semibold text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-dark-800 text-zinc-300">
                {incidents.map((incident) => (
                  <tr key={incident.id} className="hover:bg-dark-850/40 transition-colors">
                    <td className="p-4 text-zinc-500 font-bold">#{incident.id}</td>
                    <td className="p-4">
                      <div className="font-semibold text-zinc-100">{incident.title}</div>
                      <div className="text-[11px] text-zinc-500 truncate max-w-xs">{incident.description}</div>
                    </td>
                    <td className="p-4 text-orange-400 font-semibold">{incident.deviceName}</td>
                    <td className="p-4">
                      <AlertSeverityBadge severity={incident.severity} />
                    </td>
                    <td className="p-4">
                      <IncidentStatusBadge status={incident.status} />
                    </td>
                    <td className="p-4">
                      {incident.latestAiAnalysis || (incident.aiAnalyses && incident.aiAnalyses.length > 0) ? (
                        <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[11px] font-bold bg-orange-500/10 text-orange-400 border border-orange-500/30">
                          <LucideSparkles className="w-3 h-3" />
                          <span>Generated</span>
                        </span>
                      ) : (
                        <span className="text-[11px] text-zinc-500">None</span>
                      )}
                    </td>
                    <td className="p-4 text-zinc-400">
                      {new Date(incident.createdAt).toLocaleDateString()}
                    </td>
                    <td className="p-4 text-right">
                      <Link
                        to={`/incidents/${incident.id}`}
                        className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg bg-dark-950 border border-dark-800 hover:border-orange-500/50 text-orange-400 text-xs hover:text-orange-300 transition-colors"
                      >
                        <span>Investigate</span>
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
    </div>
  );
};

export default IncidentsListPage;
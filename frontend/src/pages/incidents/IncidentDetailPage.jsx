import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { incidentService, aiService } from '../../services/api';
import { IncidentStatusBadge, AlertSeverityBadge } from '../../components/common/Badge';
import {
  LucideSparkles,
  LucideArrowLeft,
  LucideAlertTriangle,
  LucideCheckCircle2,
  LucideClock,
  LucideCpu,
  LucideTerminal,
  LucideRefreshCw,
  LucideCheck,
  LucideActivity,
  LucideAlertCircle,
} from 'lucide-react';

export const IncidentDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [incident, setIncident] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [aiAnalyzing, setAiAnalyzing] = useState(false);
  const [updatingStatus, setUpdatingStatus] = useState(false);

  const fetchIncident = async () => {
    if (!id || isNaN(Number(id))) {
      navigate('/incidents', { replace: true });
      return;
    }
    try {
      setError(null);
      const res = await incidentService.getIncidentById(id);
      setIncident(res.data.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch incident details.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!id || isNaN(Number(id))) {
      navigate('/incidents', { replace: true });
      return;
    }
    fetchIncident();
  }, [id]);

  const handleRunAiAnalysis = async () => {
    try {
      setAiAnalyzing(true);
      await aiService.analyzeIncident(id);
      await fetchIncident();
    } catch (err) {
      alert(err.response?.data?.message || 'OmniRoute AI Analysis failed. Verify API key and backend logs.');
    } finally {
      setAiAnalyzing(false);
    }
  };

  const handleStatusChange = async (newStatus) => {
    try {
      setUpdatingStatus(true);
      await incidentService.updateStatus(id, newStatus);
      await fetchIncident();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update incident status.');
    } finally {
      setUpdatingStatus(false);
    }
  };

  if (loading && !incident) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="w-8 h-8 border-2 border-orange-500 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (error && !incident) {
    return (
      <div className="p-6 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 font-mono text-sm">
        {error}
      </div>
    );
  }

  const latestAiAnalysis = incident.latestAiAnalysis ||
    (incident.aiAnalyses && incident.aiAnalyses.length > 0 ? incident.aiAnalyses[incident.aiAnalyses.length - 1] : null);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/incidents')}
            className="p-2 rounded-lg bg-dark-900 border border-dark-800 text-zinc-400 hover:text-zinc-100 hover:border-dark-700 transition-colors"
          >
            <LucideArrowLeft className="w-4 h-4" />
          </button>
          <div>
            <div className="flex items-center gap-2.5">
              <h1 className="text-xl font-bold tracking-tight text-zinc-100 font-mono">
                INCIDENT #{incident.id}: {incident.title}
              </h1>
              <IncidentStatusBadge status={incident.status} />
              <AlertSeverityBadge severity={incident.severity} />
            </div>
            <p className="text-xs text-zinc-400 font-mono mt-0.5">
              Target Node: <span className="text-orange-400">{incident.deviceName}</span> | Opened: {new Date(incident.createdAt).toLocaleString()}
            </p>
          </div>
        </div>

        {/* Status Actions & AI Button */}
        <div className="flex items-center gap-3">
          <select
            value={incident.status}
            disabled={updatingStatus}
            onChange={(e) => handleStatusChange(e.target.value)}
            className="px-3 py-2 bg-dark-900 border border-dark-800 rounded-lg text-xs font-mono text-zinc-200 focus:outline-none focus:border-orange-500"
          >
            <option value="OPEN">Status: OPEN</option>
            <option value="IN_PROGRESS">Status: IN_PROGRESS</option>
            <option value="RESOLVED">Status: RESOLVED</option>
            <option value="CLOSED">Status: CLOSED</option>
          </select>

          <button
            onClick={handleRunAiAnalysis}
            disabled={aiAnalyzing}
            className="flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-orange-600 to-amber-600 hover:from-orange-500 hover:to-amber-500 text-white text-xs font-semibold rounded-lg shadow-lg shadow-orange-950/50 transition-all font-mono uppercase tracking-wider disabled:opacity-50"
          >
            <LucideSparkles className={`w-4 h-4 ${aiAnalyzing ? 'animate-spin' : ''}`} />
            <span>{aiAnalyzing ? 'Analyzing with OmniRoute...' : 'Run AI Incident Triage'}</span>
          </button>
        </div>
      </div>

      {/* Incident Description Card */}
      <div className="p-5 rounded-xl bg-dark-900/60 border border-dark-800 text-xs font-mono">
        <h2 className="text-zinc-400 uppercase tracking-wider mb-1 font-bold">Operational Context & Details</h2>
        <p className="text-zinc-200 leading-relaxed">{incident.description || 'No description provided.'}</p>
      </div>

      {/* OmniRoute AI Incident Report Section */}
      {latestAiAnalysis ? (
        <div className="rounded-2xl border border-orange-500/30 bg-gradient-to-b from-dark-900 to-dark-950 shadow-2xl p-6 relative overflow-hidden">
          <div className="absolute top-0 right-0 p-6 pointer-events-none opacity-10">
            <LucideSparkles className="w-36 h-36 text-orange-500" />
          </div>

          <div className="flex items-center justify-between border-b border-dark-800 pb-4 mb-6">
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-xl bg-orange-500/10 border border-orange-500/30 text-orange-400">
                <LucideSparkles className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-zinc-100 font-mono uppercase tracking-wider">
                  OmniRoute AI Root-Cause Diagnostic Report
                </h3>
                <p className="text-xs text-zinc-500 font-mono">
                  Synthesized across live telemetry snapshot & system journal logs via model: <span className="text-orange-400">{latestAiAnalysis.aiModel}</span>
                </p>
              </div>
            </div>
            <span className="text-[11px] font-mono text-zinc-500">
              {new Date(latestAiAnalysis.createdAt).toLocaleString()}
            </span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Root Cause Analysis */}
            <div className="p-4 rounded-xl bg-dark-950/80 border border-dark-800 space-y-2">
              <div className="flex items-center gap-2 text-xs font-bold text-amber-400 font-mono uppercase">
                <LucideAlertCircle className="w-4 h-4" />
                <span>Identified Root Cause</span>
              </div>
              <p className="text-xs text-zinc-300 font-mono leading-relaxed whitespace-pre-line">
                {latestAiAnalysis.rootCause}
              </p>
            </div>

            {/* Impact Assessment */}
            <div className="p-4 rounded-xl bg-dark-950/80 border border-dark-800 space-y-2">
              <div className="flex items-center gap-2 text-xs font-bold text-rose-400 font-mono uppercase">
                <LucideActivity className="w-4 h-4" />
                <span>Operational Impact Assessment</span>
              </div>
              <p className="text-xs text-zinc-300 font-mono leading-relaxed whitespace-pre-line">
                {latestAiAnalysis.impactAnalysis}
              </p>
            </div>
          </div>

          {/* Remediation Action Steps */}
          {latestAiAnalysis.recommendedActions && latestAiAnalysis.recommendedActions.length > 0 && (
            <div className="mt-6 p-4 rounded-xl bg-dark-950/80 border border-dark-800">
              <div className="flex items-center gap-2 text-xs font-bold text-emerald-400 font-mono uppercase mb-3">
                <LucideCheckCircle2 className="w-4 h-4" />
                <span>Recommended Triage & Remediation Procedures</span>
              </div>
              <ul className="space-y-2 font-mono text-xs text-zinc-300">
                {latestAiAnalysis.recommendedActions.map((step, idx) => (
                  <li key={idx} className="flex items-start gap-2.5">
                    <span className="flex-shrink-0 w-5 h-5 rounded bg-dark-900 border border-dark-800 text-orange-400 flex items-center justify-center text-[11px] font-bold">
                      {idx + 1}
                    </span>
                    <span className="mt-0.5">{step}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      ) : (
        <div className="rounded-2xl border border-dashed border-dark-700 bg-dark-900/40 p-8 text-center">
          <LucideSparkles className="w-8 h-8 text-zinc-600 mx-auto mb-3" />
          <h3 className="text-sm font-bold text-zinc-300 font-mono uppercase">
            No AI Diagnostics Executed Yet
          </h3>
          <p className="text-xs text-zinc-500 font-mono mt-1 max-w-md mx-auto mb-4">
            Click "Run AI Incident Triage" to pass the live hardware metric telemetry and logs to OmniRoute for automated root-cause extraction.
          </p>
          <button
            onClick={handleRunAiAnalysis}
            disabled={aiAnalyzing}
            className="px-5 py-2 bg-orange-600 hover:bg-orange-500 text-white text-xs font-semibold rounded-lg font-mono uppercase tracking-wider transition-all inline-flex items-center gap-2"
          >
            <LucideSparkles className="w-4 h-4" />
            <span>Launch OmniRoute Analysis</span>
          </button>
        </div>
      )}
    </div>
  );
};

export default IncidentDetailPage;
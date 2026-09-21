import React from 'react';

export const DeviceStatusBadge = ({ status }) => {
  return <StatusBadge status={status} />;
};

export const StatusBadge = ({ status }) => {
  const normalized = status ? status.toUpperCase() : 'UNKNOWN';

  switch (normalized) {
    case 'ONLINE':
      return (
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-950/80 text-emerald-400 border border-emerald-500/30">
          <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
          ONLINE
        </span>
      );
    case 'OFFLINE':
      return (
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-zinc-900 text-zinc-400 border border-zinc-700">
          <span className="w-2 h-2 rounded-full bg-zinc-500"></span>
          OFFLINE
        </span>
      );
    case 'PENDING':
      return (
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-950/80 text-amber-400 border border-amber-500/30">
          <span className="w-2 h-2 rounded-full bg-amber-500"></span>
          PENDING
        </span>
      );
    case 'UNREGISTERED':
      return (
        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold bg-red-950/80 text-red-400 border border-red-500/30">
          <span className="w-2 h-2 rounded-full bg-red-500"></span>
          UNREGISTERED
        </span>
      );
    default:
      return (
        <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-zinc-800 text-zinc-300">
          {normalized}
        </span>
      );
  }
};

export const AlertSeverityBadge = ({ severity }) => {
  return <SeverityBadge severity={severity} />;
};

export const AlertStatusBadge = ({ status }) => {
  const norm = status ? status.toUpperCase() : 'ACTIVE';
  switch (norm) {
    case 'ACTIVE':
      return (
        <span className="px-2.5 py-1 rounded-md text-xs font-bold bg-red-950/90 text-red-400 border border-red-600/50">
          ACTIVE
        </span>
      );
    case 'ACKNOWLEDGED':
      return (
        <span className="px-2.5 py-1 rounded-md text-xs font-bold bg-amber-950/90 text-amber-400 border border-amber-600/50">
          ACKNOWLEDGED
        </span>
      );
    case 'RESOLVED':
      return (
        <span className="px-2.5 py-1 rounded-md text-xs font-semibold bg-emerald-950/90 text-emerald-400 border border-emerald-600/40">
          RESOLVED
        </span>
      );
    default:
      return <span className="px-2 py-0.5 rounded text-xs bg-zinc-800 text-zinc-300">{norm}</span>;
  }
};

export const LogLevelBadge = ({ level }) => {
  const norm = level ? level.toUpperCase() : 'INFO';
  switch (norm) {
    case 'CRITICAL':
      return (
        <span className="px-2 py-0.5 rounded text-xs font-bold bg-red-950 text-red-400 border border-red-600/50">
          CRITICAL
        </span>
      );
    case 'ERROR':
      return (
        <span className="px-2 py-0.5 rounded text-xs font-bold bg-rose-950 text-rose-400 border border-rose-600/50">
          ERROR
        </span>
      );
    case 'WARNING':
    case 'WARN':
      return (
        <span className="px-2 py-0.5 rounded text-xs font-bold bg-amber-950 text-amber-400 border border-amber-600/50">
          WARN
        </span>
      );
    case 'INFO':
    default:
      return (
        <span className="px-2 py-0.5 rounded text-xs font-medium bg-zinc-800 text-zinc-300 border border-zinc-700">
          INFO
        </span>
      );
  }
};

export const SeverityBadge = ({ severity }) => {
  const norm = severity ? severity.toUpperCase() : 'INFO';
  switch (norm) {
    case 'CRITICAL':
      return (
        <span className="px-2.5 py-1 rounded-md text-xs font-bold bg-red-950/90 text-red-400 border border-red-600/40">
          CRITICAL
        </span>
      );
    case 'WARNING':
      return (
        <span className="px-2.5 py-1 rounded-md text-xs font-bold bg-amber-950/90 text-amber-400 border border-amber-600/40">
          WARNING
        </span>
      );
    case 'INFO':
    default:
      return (
        <span className="px-2.5 py-1 rounded-md text-xs font-medium bg-blue-950/90 text-blue-400 border border-blue-600/40">
          INFO
        </span>
      );
  }
};

export const IncidentStatusBadge = ({ status }) => {
  const norm = status ? status.toUpperCase() : 'OPEN';
  switch (norm) {
    case 'OPEN':
      return (
        <span className="px-2.5 py-1 rounded-md text-xs font-bold bg-red-950/90 text-red-400 border border-red-600/50 flex items-center gap-1 w-fit">
          <span className="w-1.5 h-1.5 rounded-full bg-red-500"></span>
          OPEN
        </span>
      );
    case 'IN_PROGRESS':
      return (
        <span className="px-2.5 py-1 rounded-md text-xs font-bold bg-orange-950/90 text-orange-400 border border-orange-600/50 flex items-center gap-1 w-fit">
          <span className="w-1.5 h-1.5 rounded-full bg-orange-500"></span>
          IN PROGRESS
        </span>
      );
    case 'RESOLVED':
      return (
        <span className="px-2.5 py-1 rounded-md text-xs font-semibold bg-emerald-950/90 text-emerald-400 border border-emerald-600/40 flex items-center gap-1 w-fit">
          <span className="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
          RESOLVED
        </span>
      );
    case 'CLOSED':
      return (
        <span className="px-2.5 py-1 rounded-md text-xs font-medium bg-zinc-800 text-zinc-400 border border-zinc-700 flex items-center gap-1 w-fit">
          CLOSED
        </span>
      );
    default:
      return <span className="px-2 py-0.5 rounded text-xs bg-zinc-800 text-zinc-300">{norm}</span>;
  }
};
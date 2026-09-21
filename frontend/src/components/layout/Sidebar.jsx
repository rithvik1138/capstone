import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  LucideLayoutDashboard,
  LucideServer,
  LucidePlusCircle,
  LucideBell,
  LucideAlertTriangle,
  LucideShield,
  LucideFileText,
  LucideCpu,
} from 'lucide-react';

export const Sidebar = () => {
  const { isAdmin } = useAuth();

  const navItemClass = ({ isActive }) =>
    `flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-sm font-medium transition-all ${
      isActive
        ? 'bg-orange-600/15 text-orange-400 border-l-2 border-orange-500 font-semibold'
        : 'text-zinc-400 hover:bg-dark-900 hover:text-zinc-100'
    }`;

  return (
    <aside className="w-64 border-r border-dark-800 bg-dark-950/60 min-h-[calc(100vh-4rem)] flex flex-col justify-between p-4">
      <div className="space-y-6">
        <div>
          <span className="px-3 text-[11px] font-bold tracking-wider text-zinc-500 uppercase">
            Monitoring
          </span>
          <nav className="mt-2 space-y-1">
            <NavLink to="/" end className={navItemClass}>
              <LucideLayoutDashboard className="w-4 h-4" />
              Overview
            </NavLink>
            <NavLink to="/devices" end className={navItemClass}>
              <LucideServer className="w-4 h-4" />
              My Devices
            </NavLink>
            <NavLink to="/devices/register" className={navItemClass}>
              <LucidePlusCircle className="w-4 h-4 text-orange-400" />
              Register Device
            </NavLink>
          </nav>
        </div>

        <div>
          <span className="px-3 text-[11px] font-bold tracking-wider text-zinc-500 uppercase">
            Operations
          </span>
          <nav className="mt-2 space-y-1">
            <NavLink to="/alerts" className={navItemClass}>
              <LucideBell className="w-4 h-4" />
              Active Alerts
            </NavLink>
            <NavLink to="/incidents" className={navItemClass}>
              <LucideAlertTriangle className="w-4 h-4" />
              Incidents & AI
            </NavLink>
          </nav>
        </div>

        {isAdmin && (
          <div>
            <span className="px-3 text-[11px] font-bold tracking-wider text-orange-500/80 uppercase flex items-center gap-1.5">
              <LucideShield className="w-3 h-3 text-orange-400" />
              Admin Fleet Ops
            </span>
            <nav className="mt-2 space-y-1">
              <NavLink to="/admin" end className={navItemClass}>
                <LucideCpu className="w-4 h-4" />
                Fleet Overview
              </NavLink>
              <NavLink to="/admin/devices" className={navItemClass}>
                <LucideServer className="w-4 h-4" />
                All Devices
              </NavLink>
              <NavLink to="/admin/logs" className={navItemClass}>
                <LucideFileText className="w-4 h-4" />
                Global Logs
              </NavLink>
            </nav>
          </div>
        )}
      </div>

      <div className="p-3 rounded-lg bg-dark-900/60 border border-dark-800 text-[11px] text-zinc-500">
        <div className="flex items-center justify-between font-mono mb-1">
          <span>Engine Status</span>
          <span className="text-emerald-400 font-semibold flex items-center gap-1">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-ping"></span>
            ACTIVE
          </span>
        </div>
        <p className="text-zinc-500 text-[10px]">Prometheus TSDB connected</p>
      </div>
    </aside>
  );
};

export default Sidebar;
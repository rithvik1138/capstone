import React from 'react';
import { LucideTrendingUp, LucideTrendingDown } from 'lucide-react';

export const MetricCard = ({
  title,
  value,
  unit = '',
  icon: Icon,
  subtitle,
  trend,
  color = 'orange', // 'orange' | 'emerald' | 'rose' | 'amber' | 'blue'
}) => {
  const colorStyles = {
    orange: {
      border: 'border-orange-500/20 hover:border-orange-500/40',
      iconBg: 'bg-orange-500/10 text-orange-400 border border-orange-500/20',
      valueColor: 'text-zinc-100',
    },
    emerald: {
      border: 'border-emerald-500/20 hover:border-emerald-500/40',
      iconBg: 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20',
      valueColor: 'text-zinc-100',
    },
    rose: {
      border: 'border-rose-500/20 hover:border-rose-500/40',
      iconBg: 'bg-rose-500/10 text-rose-400 border border-rose-500/20',
      valueColor: 'text-zinc-100',
    },
    amber: {
      border: 'border-amber-500/20 hover:border-amber-500/40',
      iconBg: 'bg-amber-500/10 text-amber-400 border border-amber-500/20',
      valueColor: 'text-zinc-100',
    },
    blue: {
      border: 'border-blue-500/20 hover:border-blue-500/40',
      iconBg: 'bg-blue-500/10 text-blue-400 border border-blue-500/20',
      valueColor: 'text-zinc-100',
    },
  };

  const currentStyle = colorStyles[color] || colorStyles.orange;

  return (
    <div
      className={`rounded-xl bg-dark-900/80 p-5 border transition-all duration-200 shadow-sm ${currentStyle.border}`}
    >
      <div className="flex items-center justify-between">
        <span className="text-xs font-medium uppercase tracking-wider text-zinc-400">{title}</span>
        {Icon && (
          <div className={`flex h-9 w-9 items-center justify-center rounded-lg ${currentStyle.iconBg}`}>
            <Icon className="h-5 w-5" />
          </div>
        )}
      </div>

      <div className="mt-4 flex items-baseline gap-2">
        <span className={`text-2xl font-bold font-mono tracking-tight ${currentStyle.valueColor}`}>
          {value !== null && value !== undefined ? value : '--'}
        </span>
        {unit && <span className="text-sm font-medium text-zinc-500">{unit}</span>}
      </div>

      {(subtitle || trend) && (
        <div className="mt-2 flex items-center justify-between text-xs text-zinc-500">
          {subtitle && <span>{subtitle}</span>}
          {trend !== undefined && (
            <span
              className={`flex items-center gap-1 font-medium ${
                trend >= 0 ? 'text-emerald-400' : 'text-rose-400'
              }`}
            >
              {trend >= 0 ? (
                <LucideTrendingUp className="h-3.5 w-3.5" />
              ) : (
                <LucideTrendingDown className="h-3.5 w-3.5" />
              )}
              {Math.abs(trend)}%
            </span>
          )}
        </div>
      )}
    </div>
  );
};

export default MetricCard;
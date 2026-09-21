import React from 'react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';

const CustomTooltip = ({ active, payload, label, unit }) => {
  if (active && payload && payload.length) {
    return (
      <div className="rounded-lg border border-dark-700 bg-dark-900/95 p-3 shadow-xl backdrop-blur">
        <p className="text-[11px] font-mono text-zinc-400 mb-1">{label}</p>
        <p className="text-sm font-bold text-orange-400 font-mono">
          {payload[0].value !== null ? Number(payload[0].value).toFixed(2) : '--'}
          <span className="text-xs text-zinc-400 ml-1">{unit}</span>
        </p>
      </div>
    );
  }
  return null;
};

export const MetricChart = ({
  data = [],
  dataKey = 'value',
  timeKey = 'time',
  title,
  unit = '%',
  color = '#f97316', // Orange-500
  height = 200,
  minDomain = 0,
  maxDomain = 100,
}) => {
  if (!data || data.length === 0) {
    return (
      <div
        className="flex flex-col items-center justify-center rounded-xl bg-dark-900/40 border border-dark-800 p-6 text-center"
        style={{ height }}
      >
        <p className="text-xs text-zinc-500 font-mono">Awaiting telemetry stream...</p>
      </div>
    );
  }

  const gradientId = `color-${dataKey}`;

  return (
    <div className="rounded-xl border border-dark-800 bg-dark-900/60 p-4">
      {title && (
        <div className="mb-3 flex items-center justify-between">
          <h4 className="text-xs font-semibold uppercase tracking-wider text-zinc-300 font-mono">
            {title}
          </h4>
          <span className="text-xs font-mono text-zinc-500">Unit: {unit}</span>
        </div>
      )}
      <div style={{ width: '100%', height }}>
        <ResponsiveContainer>
          <AreaChart data={data} margin={{ top: 5, right: 10, left: -20, bottom: 0 }}>
            <defs>
              <linearGradient id={gradientId} x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor={color} stopOpacity={0.4} />
                <stop offset="95%" stopColor={color} stopOpacity={0.0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#27272a" vertical={false} />
            <XAxis
              dataKey={timeKey}
              stroke="#52525b"
              fontSize={10}
              tickLine={false}
              axisLine={{ stroke: '#27272a' }}
            />
            <YAxis
              stroke="#52525b"
              fontSize={10}
              tickLine={false}
              axisLine={{ stroke: '#27272a' }}
              domain={[minDomain, maxDomain]}
            />
            <Tooltip content={<CustomTooltip unit={unit} />} />
            <Area
              type="monotone"
              dataKey={dataKey}
              stroke={color}
              strokeWidth={2}
              fillOpacity={1}
              fill={`url(#${gradientId})`}
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default MetricChart;
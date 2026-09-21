import React from 'react';
import { LucideInbox, LucidePlus } from 'lucide-react';

export const EmptyState = ({
  icon: Icon = LucideInbox,
  title = "No data found",
  description = "There are no records to display at this time.",
  actionLabel,
  onAction,
}) => {
  return (
    <div className="flex flex-col items-center justify-center p-12 text-center rounded-xl bg-dark-900/60 border border-dark-800/80 my-4">
      <div className="w-16 h-16 rounded-full bg-dark-800/90 flex items-center justify-center mb-4 text-orange-500 border border-orange-500/20 shadow-inner">
        <Icon className="w-8 h-8" />
      </div>
      <h3 className="text-lg font-semibold text-zinc-100 mb-1">{title}</h3>
      <p className="text-sm text-zinc-400 max-w-md mb-6">{description}</p>
      {actionLabel && onAction && (
        <button
          onClick={onAction}
          className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-orange-600 hover:bg-orange-500 text-white text-sm font-medium transition-colors shadow-lg shadow-orange-600/20"
        >
          <LucidePlus className="w-4 h-4" />
          {actionLabel}
        </button>
      )}
    </div>
  );
};

export default EmptyState;
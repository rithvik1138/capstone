import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { LucideActivity, LucideLogOut, LucideShield, LucideUser } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';

export const Navbar = () => {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="sticky top-0 z-30 flex h-16 w-full items-center justify-between border-b border-dark-800 bg-dark-950/80 px-6 backdrop-blur">
      <div className="flex items-center gap-3">
        <Link to="/" className="flex items-center gap-2.5">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-orange-600 text-white shadow-md shadow-orange-600/30">
            <LucideActivity className="h-5 w-5" />
          </div>
          <div className="flex flex-col">
            <span className="text-base font-bold tracking-tight text-white">
              Cloud<span className="text-orange-500">Monitor</span>
            </span>
            <span className="text-[10px] text-zinc-500 uppercase tracking-widest font-mono">
              Infra Telemetry
            </span>
          </div>
        </Link>
      </div>

      <div className="flex items-center gap-4">
        {user && (
          <div className="flex items-center gap-3 bg-dark-900 border border-dark-800 rounded-lg px-3 py-1.5">
            <div className="flex h-7 w-7 items-center justify-center rounded-full bg-orange-500/10 text-orange-400 border border-orange-500/30 text-xs font-bold">
              {user.username.charAt(0).toUpperCase()}
            </div>
            <div className="flex flex-col text-left">
              <span className="text-xs font-semibold text-zinc-200">{user.username}</span>
              <span className="text-[10px] text-zinc-400 flex items-center gap-1">
                {isAdmin ? (
                  <>
                    <LucideShield className="w-2.5 h-2.5 text-orange-400" />
                    Admin
                  </>
                ) : (
                  <>
                    <LucideUser className="w-2.5 h-2.5 text-zinc-400" />
                    User
                  </>
                )}
              </span>
            </div>
          </div>
        )}

        <button
          onClick={handleLogout}
          title="Sign out"
          className="flex h-9 w-9 items-center justify-center rounded-lg border border-dark-800 bg-dark-900 text-zinc-400 transition-colors hover:border-red-500/40 hover:bg-red-950/40 hover:text-red-400"
        >
          <LucideLogOut className="h-4 w-4" />
        </button>
      </div>
    </header>
  );
};

export default Navbar;
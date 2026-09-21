import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { LucideActivity, LucideLock, LucideUser, LucideMail, LucideAlertCircle } from 'lucide-react';

export const SignupPage = () => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { signup } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await signup(username, email, password);
      navigate('/login');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Check your inputs.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-dark-950 flex flex-col justify-center items-center px-4 selection:bg-orange-500 selection:text-white">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-orange-600/10 border border-orange-500/20 text-orange-400 mb-4 shadow-lg shadow-orange-950/40">
            <LucideActivity className="w-6 h-6" />
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-zinc-100 font-mono">
            CLOUD<span className="text-orange-500">MONITOR</span>
          </h1>
          <p className="text-xs text-zinc-500 mt-1 uppercase tracking-widest font-mono">
            Create Authorized User Account
          </p>
        </div>

        <div className="bg-dark-900 border border-dark-800 rounded-2xl p-6 sm:p-8 shadow-2xl">
          <h2 className="text-lg font-semibold text-zinc-100 mb-1">Register Account</h2>
          <p className="text-xs text-zinc-400 mb-6">Enter your information to get started.</p>

          {error && (
            <div className="mb-5 p-3.5 rounded-lg bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs flex items-start gap-2.5">
              <LucideAlertCircle className="w-4 h-4 flex-shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-medium text-zinc-400 mb-1.5 uppercase tracking-wider font-mono">
                Username
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-zinc-500">
                  <LucideUser className="w-4 h-4" />
                </div>
                <input
                  type="text"
                  required
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="Choose username"
                  className="w-full pl-9 pr-3.5 py-2.5 bg-dark-950 border border-dark-800 rounded-lg text-sm text-zinc-100 placeholder-zinc-600 focus:outline-none focus:border-orange-500 focus:ring-1 focus:ring-orange-500 transition-colors"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-medium text-zinc-400 mb-1.5 uppercase tracking-wider font-mono">
                Email Address
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-zinc-500">
                  <LucideMail className="w-4 h-4" />
                </div>
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="user@example.com"
                  className="w-full pl-9 pr-3.5 py-2.5 bg-dark-950 border border-dark-800 rounded-lg text-sm text-zinc-100 placeholder-zinc-600 focus:outline-none focus:border-orange-500 focus:ring-1 focus:ring-orange-500 transition-colors"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-medium text-zinc-400 mb-1.5 uppercase tracking-wider font-mono">
                Password
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-zinc-500">
                  <LucideLock className="w-4 h-4" />
                </div>
                <input
                  type="password"
                  required
                  minLength={6}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full pl-9 pr-3.5 py-2.5 bg-dark-950 border border-dark-800 rounded-lg text-sm text-zinc-100 placeholder-zinc-600 focus:outline-none focus:border-orange-500 focus:ring-1 focus:ring-orange-500 transition-colors"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full mt-2 py-2.5 px-4 bg-orange-600 hover:bg-orange-500 disabled:opacity-50 text-white text-sm font-semibold rounded-lg transition-all duration-150 shadow-lg shadow-orange-950/50 flex items-center justify-center gap-2"
            >
              {loading ? (
                <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                'Create Account'
              )}
            </button>
          </form>

          <div className="mt-6 pt-5 border-t border-dark-800 text-center">
            <p className="text-xs text-zinc-500">
              Already have an account?{' '}
              <Link to="/login" className="text-orange-400 hover:text-orange-300 font-medium">
                Sign in
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SignupPage;
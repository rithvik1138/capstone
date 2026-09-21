import React, { useState } from 'react';
import { deviceService } from '../../services/api';
import {
  LucideX,
  LucideLaptop,
  LucideServer,
  LucideCpu,
  LucideCloud,
  LucideCopy,
  LucideCheck,
  LucideTerminal,
} from 'lucide-react';

export const RegisterDeviceModal = ({ isOpen, onClose, onDeviceCreated }) => {
  const [deviceName, setDeviceName] = useState('');
  const [deviceType, setDeviceType] = useState('LAPTOP_WINDOWS');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [registeredData, setRegisteredData] = useState(null);
  const [copied, setCopied] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const res = await deviceService.registerDevice({ deviceName, deviceType });
      setRegisteredData(res.data.data);
      if (onDeviceCreated) onDeviceCreated(res.data.data.device);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to register device.');
    } finally {
      setLoading(false);
    }
  };

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleClose = () => {
    setRegisteredData(null);
    setDeviceName('');
    setError('');
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
      <div className="w-full max-w-lg rounded-2xl bg-dark-900 border border-dark-700 shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-dark-800 px-6 py-4">
          <h3 className="text-base font-semibold text-zinc-100 font-mono">
            {registeredData ? 'AGENT INSTALLATION INSTRUCTIONS' : 'REGISTER NEW DEVICE'}
          </h3>
          <button
            onClick={handleClose}
            className="rounded-lg p-1.5 text-zinc-400 hover:bg-dark-800 hover:text-zinc-200 transition-colors"
          >
            <LucideX className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6">
          {error && (
            <div className="mb-4 rounded-lg bg-rose-500/10 border border-rose-500/20 p-3 text-xs text-rose-400">
              {error}
            </div>
          )}

          {!registeredData ? (
            <form onSubmit={handleSubmit} className="space-y-5">
              <div>
                <label className="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase font-mono tracking-wider">
                  Device Name / Identifier
                </label>
                <input
                  type="text"
                  required
                  value={deviceName}
                  onChange={(e) => setDeviceName(e.target.value)}
                  placeholder="e.g. Dell-Precision-Workstation"
                  className="w-full px-3.5 py-2.5 bg-dark-950 border border-dark-800 rounded-lg text-sm text-zinc-100 placeholder-zinc-600 focus:outline-none focus:border-orange-500 transition-colors"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-zinc-400 mb-2 uppercase font-mono tracking-wider">
                  Select Infrastructure Architecture
                </label>
                <div className="grid grid-cols-2 gap-3">
                  {[
                    { type: 'LAPTOP_WINDOWS', label: 'Windows PC', icon: LucideLaptop },
                    { type: 'LAPTOP_LINUX', label: 'Linux Host', icon: LucideServer },
                    { type: 'RASPBERRY_PI_4', label: 'Raspberry Pi 4', icon: LucideCpu },
                    { type: 'AWS_EC2', label: 'AWS EC2 Node', icon: LucideCloud },
                  ].map((item) => {
                    const Icon = item.icon;
                    const isSelected = deviceType === item.type;
                    return (
                      <button
                        type="button"
                        key={item.type}
                        onClick={() => setDeviceType(item.type)}
                        className={`flex items-center gap-3 p-3 rounded-xl border text-left transition-all ${
                          isSelected
                            ? 'bg-orange-500/10 border-orange-500 text-orange-400'
                            : 'bg-dark-950 border-dark-800 text-zinc-400 hover:border-dark-700'
                        }`}
                      >
                        <Icon className="w-5 h-5 flex-shrink-0" />
                        <span className="text-xs font-medium">{item.label}</span>
                      </button>
                    );
                  })}
                </div>
              </div>

              <div className="pt-2 flex justify-end gap-3">
                <button
                  type="button"
                  onClick={handleClose}
                  className="px-4 py-2 text-xs font-medium text-zinc-400 hover:text-zinc-200"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="px-5 py-2 bg-orange-600 hover:bg-orange-500 disabled:opacity-50 text-white text-xs font-semibold rounded-lg shadow-lg shadow-orange-950/50 transition-all font-mono uppercase tracking-wider"
                >
                  {loading ? 'Registering...' : 'Generate Agent Token'}
                </button>
              </div>
            </form>
          ) : (
            <div className="space-y-4">
              <div className="rounded-lg bg-emerald-500/10 border border-emerald-500/20 p-3 text-xs text-emerald-400">
                Device registered in <span className="font-mono font-bold">PENDING</span> state.
                Run the agent command below on your machine to begin real telemetry ingestion.
              </div>

              <div>
                <label className="block text-xs font-mono text-zinc-400 mb-1">
                  Single-Line Terminal Command:
                </label>
                <div className="relative flex items-center">
                  <pre className="w-full overflow-x-auto rounded-lg bg-dark-950 border border-dark-800 p-3 text-xs font-mono text-orange-300 whitespace-pre-wrap break-all">
                    {registeredData.installCommand ||
                      (deviceType.includes('WINDOWS')
                        ? registeredData.windowsInstallCommand
                        : registeredData.linuxInstallCommand) ||
                      registeredData.linuxInstallCommand ||
                      registeredData.windowsInstallCommand}
                  </pre>
                  <button
                    onClick={() =>
                      copyToClipboard(
                        registeredData.installCommand ||
                          (deviceType.includes('WINDOWS')
                            ? registeredData.windowsInstallCommand
                            : registeredData.linuxInstallCommand) ||
                          registeredData.linuxInstallCommand ||
                          registeredData.windowsInstallCommand
                      )
                    }
                    className="absolute right-2 top-2 p-1.5 rounded bg-dark-800 hover:bg-dark-700 text-zinc-300 transition-colors"
                    title="Copy command"
                  >
                    {copied ? (
                      <LucideCheck className="w-4 h-4 text-emerald-400" />
                    ) : (
                      <LucideCopy className="w-4 h-4" />
                    )}
                  </button>
                </div>
              </div>

              <div>
                <label className="block text-xs font-mono text-zinc-400 mb-1">
                  Device Authentication Token:
                </label>
                <div className="flex items-center gap-2">
                  <input
                    type="text"
                    readOnly
                    value={registeredData.agentInstallToken || registeredData.agentToken || ''}
                    className="w-full px-3 py-1.5 bg-dark-950 border border-dark-800 rounded font-mono text-xs text-zinc-300"
                  />
                  <button
                    onClick={() =>
                      copyToClipboard(
                        registeredData.agentInstallToken || registeredData.agentToken || ''
                      )
                    }
                    className="p-2 rounded bg-dark-800 hover:bg-dark-700 text-zinc-300"
                  >
                    <LucideCopy className="w-4 h-4" />
                  </button>
                </div>
              </div>

              <div className="pt-3 flex justify-end">
                <button
                  onClick={handleClose}
                  className="px-5 py-2 bg-orange-600 hover:bg-orange-500 text-white text-xs font-semibold rounded-lg font-mono uppercase tracking-wider"
                >
                  Done
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default RegisterDeviceModal;
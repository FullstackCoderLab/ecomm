import React, { useState, useEffect } from 'react';
import { checkGatewayHealth, GATEWAY_BASE_URL } from '../services/api';
import { Server, Activity, CheckCircle2, AlertTriangle } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function GatewayStatus() {
  const [status, setStatus] = useState('checking');
  const { gatewayMeta } = useAuth();

  const verifyHealth = async () => {
    const health = await checkGatewayHealth();
    setStatus(health.ok ? 'online' : 'offline');
  };

  useEffect(() => {
    verifyHealth();
    const interval = setInterval(verifyHealth, 15000); // Check every 15s
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="flex flex-wrap items-center gap-3 text-xs">
      {/* Gateway Connection Badge */}
      <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-slate-100 border border-slate-200 shadow-sm">
        <Server className="w-3.5 h-3.5 text-slate-500" />
        <span className="font-medium text-slate-600">Gateway:</span>
        <code className="text-slate-800 font-mono text-[11px]">{GATEWAY_BASE_URL}</code>
        {status === 'online' ? (
          <span className="inline-flex items-center gap-1 text-emerald-600 font-semibold pl-1">
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
            Online
          </span>
        ) : status === 'offline' ? (
          <span className="inline-flex items-center gap-1 text-rose-600 font-semibold pl-1">
            <span className="w-2 h-2 rounded-full bg-rose-500"></span>
            Offline
          </span>
        ) : (
          <span className="text-slate-400 pl-1">Checking...</span>
        )}
      </div>

      {/* Load Balanced Instance Tag (visible when request has been made) */}
      {gatewayMeta?.loadBalancedInstance && gatewayMeta.loadBalancedInstance !== 'N/A' && (
        <div className="hidden sm:flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-indigo-50 border border-indigo-200 text-indigo-700 font-medium">
          <Activity className="w-3.5 h-3.5 text-indigo-600" />
          <span>Routed via:</span>
          <span className="font-mono text-indigo-900 bg-white/70 px-1.5 py-0.5 rounded text-[11px]">
            {gatewayMeta.loadBalancedInstance}
          </span>
          {gatewayMeta.gatewayDurationMs && (
            <span className="text-indigo-500 text-[10px]">({gatewayMeta.gatewayDurationMs}ms)</span>
          )}
        </div>
      )}
    </div>
  );
}

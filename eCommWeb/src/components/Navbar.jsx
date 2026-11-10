import React from 'react';
import { useAuth } from '../context/AuthContext';
import GatewayStatus from './GatewayStatus';
import { ShoppingBag, LogOut, User, ShieldCheck, ExternalLink } from 'lucide-react';
import { GATEWAY_BASE_URL } from '../services/api';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();

  return (
    <header className="sticky top-0 z-40 bg-white/90 backdrop-blur-md border-b border-slate-200 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          
          {/* Brand Logo & Name */}
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-600 to-violet-500 flex items-center justify-center text-white shadow-md shadow-indigo-200">
              <ShoppingBag className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-extrabold text-xl tracking-tight bg-gradient-to-r from-indigo-700 via-indigo-600 to-violet-600 bg-clip-text text-transparent">
                  eCommWeb
                </span>
                <span className="text-[10px] uppercase tracking-wider font-bold bg-indigo-100 text-indigo-700 px-1.5 py-0.5 rounded">
                  Microservices
                </span>
              </div>
              <p className="text-[11px] text-slate-400 hidden sm:block">
                Powered by Spring Cloud Gateway & Eureka
              </p>
            </div>
          </div>

          {/* Center: Gateway Status Indicator */}
          <div className="hidden md:block">
            <GatewayStatus />
          </div>

          {/* Right Actions: Eureka Link & User Menu */}
          <div className="flex items-center gap-3">
            {/* Eureka Server Dashboard Link */}
            <a
              href={GATEWAY_BASE_URL}
              target="_blank"
              rel="noopener noreferrer"
              className="hidden lg:inline-flex items-center gap-1.5 text-xs text-slate-500 hover:text-indigo-600 font-medium px-3 py-1.5 rounded-lg hover:bg-slate-100 transition-colors"
              title="Open Eureka Registry Dashboard"
            >
              <span>Eureka Dashboard</span>
              <ExternalLink className="w-3.5 h-3.5" />
            </a>

            {/* User Profile / Logout Button */}
            {isAuthenticated ? (
              <div className="flex items-center gap-2">
                <div className="flex items-center gap-2 pl-2 sm:pl-3 pr-2 py-1 bg-slate-100 rounded-full border border-slate-200">
                  <div className="w-6 h-6 rounded-full bg-indigo-600 text-white flex items-center justify-center text-xs font-bold uppercase">
                    {user?.name ? user.name[0] : 'U'}
                  </div>
                  <span className="text-xs font-semibold text-slate-700 hidden sm:inline max-w-[120px] truncate">
                    {user?.name || user?.email}
                  </span>
                  {user?.roles?.includes('ROLE_ADMIN') && (
                    <span className="hidden md:inline-flex items-center gap-0.5 bg-amber-100 text-amber-800 text-[10px] font-bold px-1.5 py-0.5 rounded-full">
                      <ShieldCheck className="w-3 h-3" /> Admin
                    </span>
                  )}
                  <button
                    onClick={logout}
                    className="p-1 rounded-full text-slate-500 hover:text-rose-600 hover:bg-white transition-all shadow-sm"
                    title="Log Out"
                  >
                    <LogOut className="w-4 h-4" />
                  </button>
                </div>
              </div>
            ) : null}
          </div>
        </div>

        {/* Mobile Gateway Status Bar */}
        <div className="py-2 border-t border-slate-100 md:hidden flex justify-center">
          <GatewayStatus />
        </div>
      </div>
    </header>
  );
}

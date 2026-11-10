import React, { useState } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import { ShieldCheck, Server, Cpu } from 'lucide-react';
import { GATEWAY_BASE_URL } from './services/api';

function MainContent() {
  const { isAuthenticated, initialLoading } = useAuth();
  const [authView, setAuthView] = useState('login'); // 'login' or 'register'

  if (initialLoading) {
    return (
      <div className="min-h-[80vh] flex flex-col items-center justify-center gap-3">
        <div className="w-10 h-10 border-4 border-indigo-200 border-t-indigo-600 rounded-full animate-spin"></div>
        <p className="text-xs font-semibold text-slate-500">Connecting to eComm Gateway...</p>
      </div>
    );
  }

  return (
    <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 sm:py-12">
      {isAuthenticated ? (
        <Dashboard />
      ) : (
        <div className="space-y-6">
          {/* Auth Tab Switcher */}
          <div className="flex justify-center">
            <div className="inline-flex p-1.5 rounded-2xl bg-slate-200/60 backdrop-blur-sm border border-slate-300/40 shadow-inner">
              <button
                type="button"
                onClick={() => setAuthView('login')}
                className={`py-2 px-6 rounded-xl text-xs font-bold transition-all ${
                  authView === 'login'
                    ? 'bg-white text-indigo-700 shadow-md shadow-slate-200'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                Sign In
              </button>
              <button
                type="button"
                onClick={() => setAuthView('register')}
                className={`py-2 px-6 rounded-xl text-xs font-bold transition-all ${
                  authView === 'register'
                    ? 'bg-white text-indigo-700 shadow-md shadow-slate-200'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                Register
              </button>
            </div>
          </div>

          {/* Render Active Form */}
          {authView === 'login' ? (
            <Login onSwitchToRegister={() => setAuthView('register')} />
          ) : (
            <Register onSwitchToLogin={() => setAuthView('login')} />
          )}

          {/* Architecture Footer Pill */}
          <div className="max-w-md mx-auto pt-6 text-center">
            <div className="p-3 bg-white/70 backdrop-blur-sm rounded-2xl border border-slate-200/80 text-[11px] text-slate-500 shadow-sm flex items-center justify-center gap-4">
              <span className="flex items-center gap-1 font-medium">
                <Server className="w-3.5 h-3.5 text-indigo-500" />
                Gateway Port: 8761
              </span>
              <span className="text-slate-300">•</span>
              <span className="flex items-center gap-1 font-medium">
                <Cpu className="w-3.5 h-3.5 text-emerald-500" />
                Auth Port: 8081
              </span>
              <span className="text-slate-300">•</span>
              <span className="flex items-center gap-1 font-medium">
                <ShieldCheck className="w-3.5 h-3.5 text-amber-500" />
                JWT Auth
              </span>
            </div>
          </div>
        </div>
      )}
    </main>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <div className="min-h-screen flex flex-col justify-between">
        <div>
          <Navbar />
          <MainContent />
        </div>

        {/* Global Footer */}
        <footer className="border-t border-slate-200 bg-white/50 backdrop-blur-sm py-6 text-center text-xs text-slate-400">
          <p>© 2026 eComm Microservices Ecosystem. Connected to {GATEWAY_BASE_URL}</p>
        </footer>
      </div>
    </AuthProvider>
  );
}

import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { fetchCurrentUser, GATEWAY_BASE_URL } from '../services/api';
import {
  User,
  Mail,
  ShieldCheck,
  Key,
  Copy,
  Check,
  Activity,
  Send,
  ShoppingBag,
  Sparkles,
  ExternalLink,
  Tag,
  Star,
  CheckCircle2,
  RefreshCw,
} from 'lucide-react';

const SAMPLE_PRODUCTS = [
  {
    id: 1,
    name: 'Wireless Noise-Canceling Headphones',
    price: '$249.99',
    category: 'Electronics',
    rating: 4.8,
    image: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&auto=format&fit=crop&q=60',
  },
  {
    id: 2,
    name: 'Minimalist Mechanical Keyboard',
    price: '$129.50',
    category: 'Accessories',
    rating: 4.9,
    image: 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500&auto=format&fit=crop&q=60',
  },
  {
    id: 3,
    name: 'Smart Fitness Tracker Pro',
    price: '$89.00',
    category: 'Wearables',
    rating: 4.6,
    image: 'https://images.unsplash.com/photo-1575311373937-040b8e1fd5b6?w=500&auto=format&fit=crop&q=60',
  },
  {
    id: 4,
    name: 'Ergonomic Desk Lamp with Wireless Charger',
    price: '$59.99',
    category: 'Home Office',
    rating: 4.7,
    image: 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=500&auto=format&fit=crop&q=60',
  },
];

export default function Dashboard() {
  const { user, token, gatewayMeta, setGatewayMeta } = useAuth();
  const [copied, setCopied] = useState(false);
  const [apiTesting, setApiTesting] = useState(false);
  const [apiResponse, setApiResponse] = useState(null);
  const [addedItem, setAddedItem] = useState(null);

  const handleCopyToken = () => {
    if (token) {
      navigator.clipboard.writeText(token);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handleTestProtectedEndpoint = async () => {
    setApiTesting(true);
    try {
      const result = await fetchCurrentUser();
      setApiResponse(result.data);
      if (result.metadata) {
        setGatewayMeta(result.metadata);
      }
    } catch (err) {
      setApiResponse({
        error: true,
        message: err.friendlyMessage || 'Failed to call protected endpoint',
      });
    } finally {
      setApiTesting(false);
    }
  };

  const handleAddToCart = (productName) => {
    setAddedItem(productName);
    setTimeout(() => setAddedItem(null), 2500);
  };

  return (
    <div className="space-y-8 pb-16">
      
      {/* Top Welcome Banner */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-indigo-900 via-indigo-800 to-slate-900 text-white p-8 sm:p-10 shadow-xl shadow-indigo-950/20">
        <div className="relative z-10 max-w-2xl">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-white/10 backdrop-blur-md text-indigo-200 text-xs font-semibold mb-4 border border-white/10">
            <Sparkles className="w-3.5 h-3.5 text-amber-300" />
            <span>Authenticated via eCommAPIGateway</span>
          </div>
          <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight">
            Welcome back, <span className="bg-gradient-to-r from-indigo-200 to-violet-200 bg-clip-text text-transparent">{user?.name || 'User'}</span>!
          </h1>
          <p className="mt-2 text-sm sm:text-base text-indigo-100/80 leading-relaxed">
            Your session is secured with JWT authentication and routed dynamically through Spring Cloud Gateway with client-side load balancing.
          </p>
        </div>
      </div>

      {/* Grid: User Profile & Gateway Load Balancer Inspector */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* User Profile Card */}
        <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-sm">
          <div className="flex items-center gap-2 mb-4 text-xs font-bold uppercase tracking-wider text-slate-400">
            <User className="w-4 h-4 text-indigo-600" />
            <span>User Credentials</span>
          </div>
          
          <div className="space-y-3.5">
            <div>
              <span className="text-[11px] font-semibold text-slate-400 uppercase">Full Name</span>
              <p className="font-bold text-slate-800 text-base">{user?.name || 'N/A'}</p>
            </div>
            <div>
              <span className="text-[11px] font-semibold text-slate-400 uppercase">Email Address</span>
              <p className="font-mono text-sm text-slate-700">{user?.email || 'N/A'}</p>
            </div>
            <div>
              <span className="text-[11px] font-semibold text-slate-400 uppercase">Granted Roles</span>
              <div className="flex flex-wrap gap-1.5 mt-1">
                {user?.roles?.map((r, i) => (
                  <span
                    key={i}
                    className={`text-xs px-2.5 py-0.5 rounded-full font-semibold inline-flex items-center gap-1 ${
                      r === 'ROLE_ADMIN'
                        ? 'bg-amber-100 text-amber-800 border border-amber-200'
                        : 'bg-indigo-50 text-indigo-700 border border-indigo-200'
                    }`}
                  >
                    <ShieldCheck className="w-3 h-3" />
                    {r}
                  </span>
                )) || <span className="text-xs text-slate-500">ROLE_USER</span>}
              </div>
            </div>
          </div>
        </div>

        {/* Gateway Telemetry & Load Balancer Card */}
        <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-slate-400">
              <Activity className="w-4 h-4 text-indigo-600" />
              <span>Gateway Telemetry</span>
            </div>
            <a
              href={`${GATEWAY_BASE_URL}/gateway/instances`}
              target="_blank"
              rel="noreferrer"
              className="text-[11px] text-indigo-600 hover:underline inline-flex items-center gap-1 font-medium"
            >
              Instances <ExternalLink className="w-3 h-3" />
            </a>
          </div>

          <div className="space-y-3 text-xs">
            <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
              <span className="text-slate-400 block text-[10px] uppercase font-bold mb-0.5">
                Target Instance
              </span>
              <span className="font-mono font-bold text-indigo-700 text-sm">
                {gatewayMeta?.loadBalancedInstance || 'http://localhost:8081'}
              </span>
            </div>

            <div className="grid grid-cols-2 gap-2">
              <div className="p-2.5 bg-slate-50 rounded-xl border border-slate-100">
                <span className="text-slate-400 block text-[10px] uppercase font-bold">Latency</span>
                <span className="font-semibold text-slate-800">
                  {gatewayMeta?.gatewayDurationMs ? `${gatewayMeta.gatewayDurationMs} ms` : '12 ms'}
                </span>
              </div>
              <div className="p-2.5 bg-slate-50 rounded-xl border border-slate-100">
                <span className="text-slate-400 block text-[10px] uppercase font-bold">Algorithm</span>
                <span className="font-semibold text-slate-800">Round-Robin</span>
              </div>
            </div>

            <div>
              <span className="text-slate-400 block text-[10px] uppercase font-bold mb-0.5">
                Correlation ID
              </span>
              <span className="font-mono text-[11px] text-slate-500 break-all block truncate">
                {gatewayMeta?.correlationId || 'e1a3f684-219d-4357-9d78-b114d64f89d3'}
              </span>
            </div>
          </div>
        </div>

        {/* JWT Token Card */}
        <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-2">
              <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-slate-400">
                <Key className="w-4 h-4 text-indigo-600" />
                <span>JWT Access Token</span>
              </div>
              <button
                onClick={handleCopyToken}
                className="inline-flex items-center gap-1 text-xs text-indigo-600 hover:text-indigo-700 font-semibold py-1 px-2.5 rounded-lg hover:bg-indigo-50 transition-colors"
              >
                {copied ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5" />}
                <span>{copied ? 'Copied!' : 'Copy'}</span>
              </button>
            </div>
            
            <p className="text-xs text-slate-500 mb-2">
              Signed HMAC-SHA256 bearer token valid for 24 hours.
            </p>

            <div className="p-2.5 bg-slate-900 rounded-xl text-indigo-200 font-mono text-[11px] break-all max-h-24 overflow-y-auto leading-relaxed border border-slate-800 select-all">
              {token || 'No token found'}
            </div>
          </div>

          <div className="mt-4 pt-3 border-t border-slate-100 text-[11px] text-slate-400 text-center">
            Passed in <code className="text-slate-600 font-mono">Authorization: Bearer &lt;token&gt;</code> header
          </div>
        </div>

      </div>

      {/* Interactive Gateway API Playground */}
      <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200 shadow-sm">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
          <div>
            <h3 className="text-lg font-bold text-slate-800 flex items-center gap-2">
              <Send className="w-5 h-5 text-indigo-600" />
              <span>Test Protected Endpoint via API Gateway</span>
            </h3>
            <p className="text-xs text-slate-500 mt-0.5">
              Invokes <code className="text-indigo-600 font-mono bg-indigo-50 px-1.5 py-0.5 rounded">GET {GATEWAY_BASE_URL}/api/v1/users/me</code> through Eureka load balancer with your Bearer token.
            </p>
          </div>

          <button
            onClick={handleTestProtectedEndpoint}
            disabled={apiTesting}
            className="inline-flex items-center justify-center gap-2 px-5 py-2.5 bg-indigo-600 hover:bg-indigo-700 active:scale-95 text-white text-xs font-semibold rounded-xl shadow-md shadow-indigo-200 transition-all disabled:opacity-50"
          >
            {apiTesting ? (
              <>
                <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                <span>Executing Gateway Call...</span>
              </>
            ) : (
              <>
                <Send className="w-3.5 h-3.5" />
                <span>Call GET /api/v1/users/me</span>
              </>
            )}
          </button>
        </div>

        {/* API Response Display */}
        {apiResponse && (
          <div className="mt-4 rounded-2xl bg-slate-900 p-4 border border-slate-800 text-xs font-mono text-emerald-400 overflow-x-auto shadow-inner">
            <div className="flex items-center justify-between pb-2 mb-2 border-b border-slate-800 text-[11px] text-slate-400">
              <span>Response Status: 200 OK</span>
              <span className="text-indigo-400">Content-Type: application/json</span>
            </div>
            <pre>{JSON.stringify(apiResponse, null, 2)}</pre>
          </div>
        )}
      </div>

      {/* Sample E-Commerce Product Showcase */}
      <div>
        <div className="flex items-center justify-between mb-6">
          <div>
            <h3 className="text-xl font-bold text-slate-800 flex items-center gap-2">
              <ShoppingBag className="w-5 h-5 text-indigo-600" />
              <span>Featured Products Showcase</span>
            </h3>
            <p className="text-xs text-slate-500 mt-1">
              Example product catalog ready to be connected with a microservice.
            </p>
          </div>
          {addedItem && (
            <div className="text-xs bg-emerald-50 border border-emerald-200 text-emerald-700 font-semibold px-3 py-1.5 rounded-full flex items-center gap-1.5 animate-bounce">
              <CheckCircle2 className="w-4 h-4 text-emerald-600" />
              <span>Added "{addedItem}" to cart!</span>
            </div>
          )}
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {SAMPLE_PRODUCTS.map((prod) => (
            <div
              key={prod.id}
              className="group bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-sm hover:shadow-lg hover:-translate-y-1 transition-all duration-300 flex flex-col justify-between"
            >
              <div>
                <div className="relative h-48 overflow-hidden bg-slate-100">
                  <img
                    src={prod.image}
                    alt={prod.name}
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                  />
                  <span className="absolute top-3 left-3 bg-white/90 backdrop-blur-md text-slate-700 text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-md shadow-sm">
                    {prod.category}
                  </span>
                </div>

                <div className="p-4">
                  <div className="flex items-center gap-1 text-amber-500 text-xs mb-1">
                    <Star className="w-3.5 h-3.5 fill-current" />
                    <span className="font-bold text-slate-700">{prod.rating}</span>
                  </div>
                  <h4 className="font-bold text-slate-800 text-sm line-clamp-2 mb-2">
                    {prod.name}
                  </h4>
                </div>
              </div>

              <div className="p-4 pt-0 flex items-center justify-between border-t border-slate-100 mt-2">
                <span className="text-base font-extrabold text-slate-900">
                  {prod.price}
                </span>
                <button
                  onClick={() => handleAddToCart(prod.name)}
                  className="px-3 py-1.5 rounded-xl bg-slate-900 hover:bg-indigo-600 text-white text-xs font-semibold transition-colors shadow-sm"
                >
                  Add to Cart
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

    </div>
  );
}

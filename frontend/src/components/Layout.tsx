import { Outlet, Link, useLocation } from 'react-router-dom';
import { Vote, ChevronRight } from 'lucide-react';

export default function Layout() {
  const location = useLocation();
  const isHome = location.pathname === '/';

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <header style={{
        borderBottom: '1px solid var(--border)',
        background: 'rgba(13,13,26,0.85)',
        backdropFilter: 'blur(20px)',
        position: 'sticky', top: 0, zIndex: 100,
      }}>
        <div style={{
          maxWidth: 1100, margin: '0 auto', padding: '0 24px',
          height: 64, display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        }}>
          <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <div style={{
              width: 36, height: 36, borderRadius: 10,
              background: 'linear-gradient(135deg, var(--accent), #6366f1)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              boxShadow: '0 0 20px var(--accent-glow)',
            }}>
              <Vote size={18} color="white" />
            </div>
            <span style={{ fontFamily: 'Syne', fontWeight: 700, fontSize: 17, letterSpacing: '-0.3px' }}>
              CoopVoto
            </span>
          </Link>

          {!isHome && (
            <nav style={{ display: 'flex', alignItems: 'center', gap: 6, color: 'var(--text-3)', fontSize: 13 }}>
              <Link to="/" style={{ color: 'var(--text-2)', transition: 'color 0.2s' }}
                onMouseEnter={e => (e.currentTarget.style.color = 'var(--text-1)')}
                onMouseLeave={e => (e.currentTarget.style.color = 'var(--text-2)')}>
                Pautas
              </Link>
              <ChevronRight size={14} />
              <span style={{ color: 'var(--text-1)' }}>Detalhe</span>
            </nav>
          )}

          <div style={{
            fontSize: 12, color: 'var(--text-3)',
            border: '1px solid var(--border)', borderRadius: 20, padding: '4px 12px',
          }}>
            API v1
          </div>
        </div>
      </header>

      <main style={{ flex: 1, maxWidth: 1100, margin: '0 auto', width: '100%', padding: '32px 24px' }}>
        <Outlet />
      </main>

      <footer style={{
        textAlign: 'center', padding: '20px 24px',
        borderTop: '1px solid var(--border)',
        color: 'var(--text-3)', fontSize: 13,
      }}>
        Sistema de Votação Cooperativa.
      </footer>
    </div>
  );
}

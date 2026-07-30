import { useMutation } from '@tanstack/react-query';
import { ListTodo } from 'lucide-react';
import { useState, type FormEvent } from 'react';
import toast from 'react-hot-toast';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { getErrorMessage } from '../lib/api';
import { authService } from '../services/authService';
import { useAuthStore } from '../store/authStore';

export function LoginPage() {
  const [email, setEmail] = useState('admin@taskmanager.local');
  const [password, setPassword] = useState('Admin@123');
  const token = useAuthStore((state) => state.token);
  const setSession = useAuthStore((state) => state.setSession);
  const navigate = useNavigate();
  const location = useLocation();

  const login = useMutation({
    mutationFn: () => authService.login(email, password),
    onSuccess: (data) => {
      setSession(data.token, data.user);
      toast.success('Login realizado com sucesso.');
      const target = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname ?? '/projects';
      navigate(target, { replace: true });
    },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  if (token) return <Navigate to="/projects" replace />;

  const submit = (event: FormEvent) => {
    event.preventDefault();
    login.mutate();
  };

  return (
    <main className="auth-page">
      <section className="auth-card">
        <div className="auth-brand"><ListTodo size={34} /><div><h1>Task Manager</h1><p>Organize projetos e entregue com clareza.</p></div></div>
        <form className="stack-form" onSubmit={submit}>
          <label>Email<input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required data-testid="login-email" /></label>
          <label>Senha<input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required data-testid="login-password" /></label>
          <button className="button primary full" disabled={login.isPending} data-testid="login-submit">{login.isPending ? 'Entrando...' : 'Entrar'}</button>
        </form>
        <p className="auth-footer">Ainda não tem conta? <Link to="/register">Criar conta MEMBER</Link></p>
      </section>
    </main>
  );
}

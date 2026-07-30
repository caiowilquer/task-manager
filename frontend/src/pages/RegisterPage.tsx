import { useMutation } from '@tanstack/react-query';
import { useState, type FormEvent } from 'react';
import toast from 'react-hot-toast';
import { Link, useNavigate } from 'react-router-dom';
import { getErrorMessage } from '../lib/api';
import { authService } from '../services/authService';

export function RegisterPage() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();
  const register = useMutation({
    mutationFn: () => authService.register(name, email, password),
    onSuccess: () => { toast.success('Cadastro realizado. Agora faça login.'); navigate('/login'); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });
  const submit = (e: FormEvent) => { e.preventDefault(); register.mutate(); };
  return (
    <main className="auth-page"><section className="auth-card">
      <h1>Criar conta</h1><p className="muted">Novos cadastros recebem o perfil MEMBER.</p>
      <form className="stack-form" onSubmit={submit}>
        <label>Nome<input value={name} onChange={(e) => setName(e.target.value)} required maxLength={100} /></label>
        <label>Email<input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required /></label>
        <label>Senha<input type="password" minLength={8} maxLength={72} value={password} onChange={(e) => setPassword(e.target.value)} required /></label>
        <button className="button primary full" disabled={register.isPending}>Cadastrar</button>
      </form>
      <p className="auth-footer"><Link to="/login">Voltar para o login</Link></p>
    </section></main>
  );
}

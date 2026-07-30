import { LogOut, ListTodo } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

export function AppHeader() {
  const user = useAuthStore((state) => state.user);
  const clearSession = useAuthStore((state) => state.clearSession);
  const navigate = useNavigate();

  const logout = () => {
    clearSession();
    navigate('/login');
  };

  return (
    <header className="app-header">
      <Link to="/projects" className="brand" aria-label="Task Manager">
        <ListTodo size={25} />
        <span>Task Manager</span>
      </Link>
      <div className="header-user">
        <div>
          <strong>{user?.name}</strong>
          <small>{user?.role}</small>
        </div>
        <button className="icon-button" onClick={logout} aria-label="Sair" title="Sair" data-testid="logout-button">
          <LogOut size={19} />
        </button>
      </div>
    </header>
  );
}

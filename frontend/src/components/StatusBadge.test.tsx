import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { StatusBadge } from './StatusBadge';

describe('StatusBadge', () => {
  it('renders the localized label for IN_PROGRESS', () => {
    render(<StatusBadge status="IN_PROGRESS" />);
    expect(screen.getByText('Em andamento')).toBeInTheDocument();
  });
});

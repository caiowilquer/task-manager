import { expect, test } from '@playwright/test';

const suffix = Date.now().toString();
const projectName = `E2E Project ${suffix}`;

test('ADMIN creates a critical task and MEMBER cannot close it', async ({ page }) => {
  await page.goto('/login');
  await page.getByTestId('login-email').fill('admin@taskmanager.local');
  await page.getByTestId('login-password').fill('Admin@123');
  await page.getByTestId('login-submit').click();
  await expect(page.getByRole('heading', { name: 'Seus projetos' })).toBeVisible();

  await page.getByTestId('new-project').click();
  await page.getByTestId('project-name').fill(projectName);
  await page.getByTestId('save-project').click();
  await page.getByText(projectName).click();

  await page.getByRole('button', { name: /Membros/ }).click();
  await page.getByTestId('member-email').fill('member@taskmanager.local');
  await page.getByTestId('add-member').click();
  await expect(page.getByText('member@taskmanager.local')).toBeVisible();
  await page.getByLabel('Fechar').click();

  await page.getByTestId('new-task').click();
  await page.getByTestId('task-title').fill('Critical E2E task');
  await page.getByTestId('task-priority').selectOption('CRITICAL');
  await page.getByTestId('task-assignee').selectOption({ label: 'Member User' });
  await page.getByTestId('save-task').click();
  await expect(page.getByText('Critical E2E task')).toBeVisible();

  await page.getByTestId('logout-button').click();
  await page.getByTestId('login-email').fill('member@taskmanager.local');
  await page.getByTestId('login-password').fill('Member@123');
  await page.getByTestId('login-submit').click();
  await page.getByText(projectName).click();

  const task = page.getByText('Critical E2E task');
  const doneColumn = page.getByTestId('column-DONE');
  await task.dragTo(doneColumn);
  await expect(page.getByText(/não possui permissão|Only the project ADMIN/i)).toBeVisible();
  await expect(page.getByTestId('column-TODO').getByText('Critical E2E task')).toBeVisible();
});

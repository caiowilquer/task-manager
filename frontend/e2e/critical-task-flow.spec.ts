import { expect, test } from '@playwright/test';
import type { Locator, Page } from '@playwright/test';

const suffix = Date.now().toString();
const projectName = `Projeto E2E ${suffix}`;
const criticalTaskTitle = `Tarefa crítica E2E ${suffix}`;

async function dragAndDrop(
    page: Page,
    source: Locator,
    target: Locator
): Promise<void> {
    const sourceBox = await source.boundingBox();
    const targetBox = await target.boundingBox();

    if (!sourceBox) {
        throw new Error('Não foi possível localizar a tarefa para arrastar.');
    }

    if (!targetBox) {
        throw new Error('Não foi possível localizar a coluna de destino.');
    }

    const sourceX = sourceBox.x + sourceBox.width / 2;
    const sourceY = sourceBox.y + sourceBox.height / 2;

    const targetX = targetBox.x + targetBox.width / 2;
    const targetY = targetBox.y + Math.min(targetBox.height / 2, 180);

    await page.mouse.move(sourceX, sourceY);
    await page.mouse.down();

    // Ativa o sensor de arraste do dnd-kit.
    await page.waitForTimeout(200);

    await page.mouse.move(sourceX + 20, sourceY + 5, {
        steps: 5
    });

    await page.mouse.move(targetX, targetY, {
        steps: 25
    });

    await page.waitForTimeout(300);
    await page.mouse.up();
}

test(
    'ADMIN cria tarefa crítica e MEMBER não pode finalizá-la',
    async ({ page }) => {
        test.setTimeout(60_000);

        /*
         * Login como ADMIN.
         */
        await page.goto('/login');

        await page
            .getByTestId('login-email')
            .fill('admin@taskmanager.local');

        await page
            .getByTestId('login-password')
            .fill('Admin@123');

        await page
            .getByTestId('login-submit')
            .click();

        await expect(
            page.getByRole('heading', {
                name: 'Seus projetos'
            })
        ).toBeVisible();

        /*
         * Criação do projeto.
         */
        await page
            .getByTestId('new-project')
            .click();

        await page
            .getByTestId('project-name')
            .fill(projectName);

        const createProjectResponsePromise = page.waitForResponse(
            (response) =>
                response.request().method() === 'POST' &&
                response.url().includes('/api/v1/projects') &&
                !response.url().includes('/members') &&
                !response.url().includes('/tasks'),
            {
                timeout: 10_000
            }
        );

        await page
            .getByTestId('save-project')
            .click();

        const createProjectResponse =
            await createProjectResponsePromise;

        expect([200, 201]).toContain(
            createProjectResponse.status()
        );

        const projectLink = page.getByText(projectName, {
            exact: true
        });

        await expect(projectLink).toBeVisible({
            timeout: 10_000
        });

        await projectLink.click();

        /*
         * Adição do MEMBER ao projeto.
         */
        await page
            .getByRole('button', {
                name: /membros/i
            })
            .click();

        await page
            .getByTestId('member-email')
            .fill('member@taskmanager.local');

        const addMemberResponsePromise = page.waitForResponse(
            (response) =>
                response.request().method() === 'POST' &&
                response.url().includes('/projects/') &&
                response.url().includes('/members'),
            {
                timeout: 10_000
            }
        );

        await page
            .getByTestId('add-member')
            .click();

        const addMemberResponse =
            await addMemberResponsePromise;

        expect([200, 201]).toContain(
            addMemberResponse.status()
        );

        const memberResponseBody =
            await addMemberResponse.json();

        const addedMemberEmail =
            memberResponseBody?.user?.email ??
            memberResponseBody?.email;

        expect(addedMemberEmail).toBe(
            'member@taskmanager.local'
        );

        await page
            .getByLabel('Fechar')
            .click();

        /*
         * Criação da tarefa CRITICAL atribuída ao MEMBER.
         */
        await page
            .getByTestId('new-task')
            .click();

        await page
            .getByTestId('task-title')
            .fill(criticalTaskTitle);

        await page
            .getByTestId('task-priority')
            .selectOption('CRITICAL');

        await page
            .getByTestId('task-assignee')
            .selectOption({
                label: 'Member User'
            });

        const createTaskResponsePromise = page.waitForResponse(
            (response) =>
                response.request().method() === 'POST' &&
                response.url().includes('/projects/') &&
                response.url().includes('/tasks'),
            {
                timeout: 10_000
            }
        );

        await page
            .getByTestId('save-task')
            .click();

        const createTaskResponse =
            await createTaskResponsePromise;

        expect([200, 201]).toContain(
            createTaskResponse.status()
        );

        const adminTodoColumn =
            page.getByTestId('column-TODO');

        await expect(
            adminTodoColumn.getByText(
                criticalTaskTitle,
                {
                    exact: true
                }
            )
        ).toBeVisible({
            timeout: 10_000
        });

        /*
         * Logout do ADMIN.
         */
        await page
            .getByTestId('logout-button')
            .click();

        /*
         * Login como MEMBER.
         */
        await page
            .getByTestId('login-email')
            .fill('member@taskmanager.local');

        await page
            .getByTestId('login-password')
            .fill('Member@123');

        await page
            .getByTestId('login-submit')
            .click();

        await expect(
            page.getByRole('heading', {
                name: 'Seus projetos'
            })
        ).toBeVisible();

        /*
         * Acessa o projeto criado pelo ADMIN.
         */
        const memberProjectLink = page.getByText(
            projectName,
            {
                exact: true
            }
        );

        await expect(memberProjectLink).toBeVisible({
            timeout: 10_000
        });

        await memberProjectLink.click();

        /*
         * Localiza a tarefa e as colunas do quadro.
         */
        const memberTodoColumn =
            page.getByTestId('column-TODO');

        const doneColumn =
            page.getByTestId('column-DONE');

        const task = memberTodoColumn.getByText(
            criticalTaskTitle,
            {
                exact: true
            }
        );

        await expect(task).toBeVisible({
            timeout: 10_000
        });

        /*
         * Aguarda a requisição de alteração de status antes
         * de iniciar o drag and drop.
         */
        const updateStatusResponsePromise =
            page.waitForResponse(
                (response) =>
                    response.request().method() === 'PATCH' &&
                    response.url().includes('/tasks/') &&
                    response.url().includes('/status'),
                {
                    timeout: 15_000
                }
            );

        /*
         * MEMBER tenta mover a tarefa CRITICAL para DONE.
         */
        await dragAndDrop(
            page,
            task,
            doneColumn
        );

        const updateStatusResponse =
            await updateStatusResponsePromise;

        /*
         * O backend deve negar a operação.
         */
        expect(updateStatusResponse.status()).toBe(403);

        const errorResponseBody =
            await updateStatusResponse.json();

        const errorMessage =
            errorResponseBody?.detail ??
            errorResponseBody?.message ??
            errorResponseBody?.title ??
            '';

        expect(errorMessage).toMatch(
            /admin|permissão|crítica|proprietário/i
        );

        /*
         * A tarefa deve continuar na coluna TODO.
         */
        await expect(
            memberTodoColumn.getByText(
                criticalTaskTitle,
                {
                    exact: true
                }
            )
        ).toBeVisible({
            timeout: 10_000
        });

        /*
         * A tarefa não pode aparecer em DONE.
         */
        await expect(
            doneColumn.getByText(
                criticalTaskTitle,
                {
                    exact: true
                }
            )
        ).not.toBeVisible();
    }
);
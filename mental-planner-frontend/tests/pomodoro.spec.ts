import { test, expect } from '@playwright/test';

test.describe('Pomodoro Timer Page', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/pomodoro', { waitUntil: 'domcontentloaded' });
    // Wait for the page heading to ensure the page is ready
    await page.getByRole('heading', { level: 1, name: /pomodoro timer/i }).waitFor({
      state: 'visible',
      timeout: 10000,
    });
  });

  test('should display the page header', async ({ page }) => {
    // Prefer role-based heading selector (robust against emoji/text changes)
    await expect(
      page.getByRole('heading', { level: 1, name: /pomodoro timer/i })
    ).toBeVisible();
    await expect(page.getByText(/focus and track your productivity sessions/i)).toBeVisible();
  });

  test('should display the timer with initial time', async ({ page }) => {
    // Prefer role=timer; fallback to aria-live region if timer role isn't present
    let timerDisplay = page.getByRole('timer').first();
    if ((await timerDisplay.count()) === 0) {
      timerDisplay = page.locator('[aria-live="polite"]');
    }

    await timerDisplay.waitFor({ state: 'visible', timeout: 10000 });
    await expect(timerDisplay).toBeVisible();
    // Ensure the displayed text looks like MM:SS or M:SS
    await expect(timerDisplay).toHaveText(/\d{1,2}:\d{2}/);
  });

  test('should have timer control buttons', async ({ page }) => {
    // Use accessible names for icon-only buttons
    const startButton = page.getByRole('button', { name: /start timer|start/i });
    await expect(startButton).toBeVisible();

    const resetButton = page.getByRole('button', { name: /reset timer|reset/i });
    await expect(resetButton).toBeVisible();
  });

  test('should have time input field', async ({ page }) => {
    // Prefer spinbutton role with a label like 'Timer duration in minutes', then fallback
    let timeInput = page.getByRole('spinbutton', { name: /duration|minutes|timer duration/i }).first();
    if ((await timeInput.count()) === 0) {
      timeInput = page.locator('input[type="number"]').first();
    }
    await expect(timeInput).toBeVisible();
  });

  test('should allow changing the timer duration', async ({ page }) => {
    let timeInput = page.getByRole('spinbutton', { name: /duration|minutes|timer duration/i }).first();
    if ((await timeInput.count()) === 0) {
      timeInput = page.locator('input[type="number"]').first();
    }

    // Wait for input to be visible and interactable
    await timeInput.waitFor({ state: 'visible', timeout: 5000 });

    // Click to focus, select all, and type new value
    await timeInput.click();
    await timeInput.selectText();
    await timeInput.pressSequentially('10', { delay: 50 });

    // The input should show the new value
    await expect(timeInput).toHaveValue('10', { timeout: 3000 });
  });

  test('should start the timer when Start button is clicked', async ({ page }) => {
    let timerDisplay = page.getByRole('timer').first();
    if ((await timerDisplay.count()) === 0) {
      timerDisplay = page.locator('[aria-live="polite"]');
    }
    await timerDisplay.waitFor({ state: 'visible', timeout: 10000 });
    const initialTime = (await timerDisplay.textContent())?.trim() ?? '';

    const startButton = page.getByRole('button', { name: /start timer|start/i }).first();
    await expect(startButton).toBeVisible({ timeout: 5000 });
    await startButton.click();

    // Wait for the timer display to change — Playwright will auto-retry for up to 7s
    await expect(timerDisplay).not.toHaveText(initialTime, { timeout: 7000 });
  });

  test('should pause the timer when Pause button is clicked', async ({ page }) => {
    let timerDisplay = page.getByRole('timer').first();
    if ((await timerDisplay.count()) === 0) {
      timerDisplay = page.locator('[aria-live="polite"]');
    }
    await timerDisplay.waitFor({ state: 'visible', timeout: 10000 });
    const beforeStart = (await timerDisplay.textContent())?.trim() ?? '';

    // Start the timer
    const startButton = page.getByRole('button', { name: /start timer|start/i }).first();
    await expect(startButton).toBeVisible({ timeout: 5000 });
    await startButton.click();

    // Wait until the timer display changes to confirm it's running
    await expect(timerDisplay).not.toHaveText(beforeStart, { timeout: 7000 });

    // Click Pause (the Start button toggles to Pause while running)
    const pauseButton = page.getByRole('button', { name: /pause timer|pause/i }).first();
    await expect(pauseButton).toBeVisible({ timeout: 3000 });
    await pauseButton.click();

    // Verify that the timer is paused by checking the display doesn't change
    const pausedTime = (await timerDisplay.textContent())?.trim();
    await page.waitForTimeout(1200);
    const stillPausedTime = (await timerDisplay.textContent())?.trim();
    expect(stillPausedTime).toBe(pausedTime);
  });

  test('should reset the timer when Reset button is clicked', async ({ page }) => {
    let timeInput = page.getByRole('spinbutton', { name: /duration|minutes|timer duration/i }).first();
    if ((await timeInput.count()) === 0) {
      timeInput = page.locator('input[type="number"]').first();
    }
    await expect(timeInput).toBeVisible({ timeout: 5000 });
    await timeInput.click();
    await timeInput.selectText();
    await timeInput.pressSequentially('5', { delay: 50 });
    await expect(timeInput).toHaveValue('5', { timeout: 3000 });

    // Start the timer
    const startButton = page.getByRole('button', { name: /start timer|start/i });
    await expect(startButton).toBeVisible({ timeout: 5000 });
    await startButton.click();

    // Wait for the timer to tick at least once
    let timerDisplay = page.getByRole('timer').first();
    if ((await timerDisplay.count()) === 0) {
      timerDisplay = page.locator('[aria-live="polite"]');
    }
    await expect(timerDisplay).not.toHaveText('05:00', { timeout: 7000 });

    // Click reset
    const resetButton = page.getByRole('button', { name: /reset timer|reset/i });
    await expect(resetButton).toBeVisible({ timeout: 5000 });
    await resetButton.click();

    // Verify timer shows the original input time (5:00)
    await expect(timerDisplay).toHaveText(/5:00/, { timeout: 3000 });
  });

  test('should disable time input when timer is running', async ({ page }) => {
    let timeInput = page.getByRole('spinbutton', { name: /duration|minutes|timer duration/i }).first();
    if ((await timeInput.count()) === 0) {
      timeInput = page.locator('input[type="number"]').first();
    }
    await expect(timeInput).toBeVisible({ timeout: 5000 });
    await expect(timeInput).toBeEnabled();

    // Start the timer
    const startButton = page.getByRole('button', { name: /start timer|start/i }).first();
    await expect(startButton).toBeVisible({ timeout: 5000 });
    await startButton.click();

    // The input should become disabled once the timer is running
    await expect(timeInput).toBeDisabled({ timeout: 6000 });
  });


  test('should display session settings on desktop view', async ({ page }) => {
    // Set desktop viewport
    await page.setViewportSize({ width: 1280, height: 720 });

    // Check for session settings heading
    await expect(
      page.getByRole('heading', { level: 3, name: /session settings/i })
    ).toBeVisible();
  });

  test('should be responsive on mobile', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });

    // Main elements should still be visible (use heading role)
    await expect(page.getByRole('heading', { level: 1, name: /pomodoro timer/i })).toBeVisible();

    // Timer display should be visible
    let timerDisplay = page.getByRole('timer').first();
    if ((await timerDisplay.count()) === 0) {
      timerDisplay = page.locator('[aria-live="polite"]');
    }
    await timerDisplay.waitFor({ state: 'visible', timeout: 10000 });
    await expect(timerDisplay).toBeVisible();

    // Control buttons should be visible
    await expect(page.getByRole('button', { name: /start timer|start|pause timer|pause/i })).toBeVisible();
    await expect(page.getByRole('button', { name: /reset timer|reset/i })).toBeVisible();
  });

  test('should have proper visual feedback when timer is running', async ({ page }) => {
    const startButton = page.getByRole('button', { name: /start timer|start/i }).first();
    await expect(startButton).toBeVisible({ timeout: 5000 });
    await expect(startButton).toBeEnabled();

    await startButton.click();

    // After clicking Start, the button should toggle to show a Pause control
    const pauseButton = page.getByRole('button', { name: /pause timer|pause/i }).first();
    await expect(pauseButton).toBeVisible({ timeout: 10000 });
  });

  test('should handle navigation back to home', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveURL('/');
  });
});

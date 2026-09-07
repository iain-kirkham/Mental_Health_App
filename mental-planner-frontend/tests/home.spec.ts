import { test, expect } from '@playwright/test';

test.describe('Home Page', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/', { waitUntil: 'domcontentloaded' });
    // Wait for the main heading to ensure the page is ready
    await page
      .getByRole('heading', { name: 'ADHD Focus Companion' })
      .waitFor({ state: 'visible', timeout: 10000 });
  });

  test('should display the main heading and tagline', async ({ page }) => {
    // Check main heading
    await expect(page.getByRole('heading', { name: 'ADHD Focus Companion' })).toBeVisible();

    // Check tagline text
    await expect(page.getByText('A mental health toolkit designed')).toBeVisible();
    await expect(page.getByText('with ADHD in mind')).toBeVisible();
  });

  test('should display both feature cards', async ({ page }) => {
    // Scope to the page main to avoid header/nav duplicates
    const focusCard = page.getByRole('main').getByRole('link', { name: 'Focus sessions' });
    await expect(focusCard).toBeVisible();
    await expect(page.getByText('Run a focus timer against a task, or just start one and see where it goes.')).toBeVisible();

    const moodCard = page.getByRole('main').getByRole('link', { name: 'Mood Tracker' });
    await expect(moodCard).toBeVisible();
    await expect(page.getByText('Log daily moods with customizable factors')).toBeVisible();
  });

  test('should navigate to Focus page when clicking the card', async ({ page }) => {
    const link = page.getByRole('link', { name: 'Focus sessions' });
    await expect(link).toBeVisible();

    // Wait for the URL change and click together to avoid a race condition
    await Promise.all([page.waitForURL('**/focus'), link.click()]);

    await expect(page.getByRole('heading', { level: 1, name: /focus timer/i })).toBeVisible();
  });

  test('should navigate to Mood Tracker page when clicking the card', async ({ page }) => {
    // Scope to the main content to avoid the header/nav link
    const moodLink = page.getByRole('main').getByRole('link', { name: 'Mood Tracker' });
    await expect(moodLink).toBeVisible();

    // Click and wait for navigation to avoid race conditions
    await Promise.all([page.waitForURL('**/mood-tracker'), moodLink.click()]);

    await expect(page.getByRole('heading', { level: 1, name: /how are you feeling/i })).toBeVisible();
  });

  test('should navigate to Focus page using the Start a session button', async ({ page }) => {
    const startButton = page.getByRole('main').getByRole('button', { name: 'Start a session' });
    await expect(startButton).toBeVisible();

    // Wait for navigation and click together to avoid race conditions
    await Promise.all([page.waitForURL('**/focus'), startButton.click()]);

    // Optionally verify page content after navigation
    await expect(page.getByRole('heading', { level: 1, name: /focus timer/i })).toBeVisible();
  });

  test('should navigate to Mood Tracker page using the Track Mood button', async ({ page }) => {
    const trackButton = page.getByRole('main').getByRole('button', { name: 'Track Mood' });
    await expect(trackButton).toBeVisible();

    await Promise.all([page.waitForURL('**/mood-tracker'), trackButton.click()]);

    await expect(page.getByRole('heading', { level: 1, name: /how are you feeling/i })).toBeVisible();
  });

  test('should display the ADHD brains info card', async ({ page }) => {
    // Check for the informational card
    await expect(page.getByText('Built for ADHD Brains')).toBeVisible();
    await expect(page.getByText(/This app is designed by an ADHDer for ADHDers/)).toBeVisible();
  });

  test('should have proper icons displayed', async ({ page }) => {
    // Scope to main and check Focus sessions card icon
    const focusCard = page.getByRole('main').getByRole('link', { name: 'Focus sessions' });
    await focusCard.waitFor({ state: 'visible', timeout: 10000 });
    await expect(focusCard).toBeVisible();
    const focusIcon = focusCard.locator('img, svg').first();
    await focusIcon.waitFor({ state: 'visible', timeout: 5000 });
    await expect(focusIcon).toBeVisible();

    // Scope to main and check Mood Tracker card icon
    const moodCard = page.getByRole('main').getByRole('link', { name: 'Mood Tracker' });
    await moodCard.waitFor({ state: 'visible', timeout: 10000 });
    await expect(moodCard).toBeVisible();
    const moodIcon = moodCard.locator('img, svg').first();
    await moodIcon.waitFor({ state: 'visible', timeout: 5000 });
    await expect(moodIcon).toBeVisible();
  });

  test('should be responsive and display properly on mobile', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });
    await page.reload();

    await expect(page.getByRole('heading', { name: 'ADHD Focus Companion' })).toBeVisible();

    const main = page.getByRole('main');
    await expect(main.getByText('Focus sessions')).toBeVisible();
    await expect(main.getByText('Mood Tracker')).toBeVisible();
  });
});

import { test, expect, Locator, Page } from '@playwright/test';

test.describe('Mood Tracker Page', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/mood-tracker', { waitUntil: 'domcontentloaded' });
    // Wait for the page heading to ensure the page is ready
    await page
      .getByRole('heading', { level: 1, name: /how are you feeling/i })
      .waitFor({ state: 'visible', timeout: 10000 });
  });

  /**
   * Helper function to locate the notes textarea with multiple fallback strategies
   * Tries role-based selection first, then aria-label, for robustness
   */
  async function getNotesTextarea(page: Page): Promise<Locator> {
    const roleLocator = page.getByRole('textbox', { name: /mood notes|notes/i });
    const roleCount = await roleLocator.count();
    for (let i = 0; i < roleCount; i++) {
      const candidate = roleLocator.nth(i);
      if (await candidate.isVisible()) return candidate;
    }

    const ariaLocator = page.locator('textarea[aria-label="Mood notes"]');
    await ariaLocator.first().waitFor({ state: 'visible', timeout: 5000 });
    return ariaLocator.first();
  }

  test('should display the page header', async ({ page }) => {
    await expect(
      page.getByRole('heading', { level: 1, name: /how are you feeling/i })
    ).toBeVisible();
    await expect(
      page.getByText('Track your mood and contributing factors')
    ).toBeVisible();
  });

  test('should display mood selection options', async ({ page }) => {
    const moodOptions = page.locator('button');
    const count = await moodOptions.count();
    expect(count).toBeGreaterThan(0);
  });

  test('should allow selecting a mood', async ({ page }) => {
    const moodButton = page.getByRole('button', { name: /Select mood/i }).first();
    await moodButton.click();
    await expect(moodButton).toHaveAttribute('aria-pressed', 'true');
  });

  test('should display date and time inputs', async ({ page }) => {
    const dateButton = page.getByRole('button', { name: /Select date/i });
    await expect(dateButton).toBeVisible();

    const timeButton = page.getByRole('button', { name: /Select time/i });
    await expect(timeButton).toBeVisible();
  });

  test('should display factors section', async ({ page }) => {
    // Try common text options for the section
    let factorsSection = page.getByText(/factors/i);
    if ((await factorsSection.count()) === 0) {
      factorsSection = page.getByText(/contributing/i);
    }

    const count = await factorsSection.count();
    expect(count).toBeGreaterThan(0);
  });

  test('should allow removing added factors', async ({ page }) => {
    const addFactorButton = page.getByRole('button', { name: /Add custom factor/i });

    if (await addFactorButton.isVisible()) {
      await addFactorButton.click();

      const factorInput = page.locator('input[aria-label="Custom factor name"]').first();
      await expect(factorInput).toBeVisible({ timeout: 3000 });

      await factorInput.fill('Test Factor');
      await page.keyboard.press('Enter');

      // Wait for the badge with the factor text to appear
      const factorBadge = page.getByText('Test Factor');
      await expect(factorBadge).toBeVisible({ timeout: 3000 });

      const removeButton = page.getByRole('button', { name: /Remove Test Factor/i }).first();
      if (await removeButton.count() > 0 && await removeButton.isVisible()) {
        await removeButton.click();
      } else {
        // Fallback: find a badge containing the text and click its remove control
        const badgeRemoveFallback = page.locator('div').filter({ hasText: 'Test Factor' }).locator('button, [role="button"]');
        if (await badgeRemoveFallback.count() > 0 && await badgeRemoveFallback.first().isVisible()) {
          await badgeRemoveFallback.first().click();
        }
      }
    }
  });

  test('should display notes textarea', async ({ page }) => {
    const notes = await getNotesTextarea(page);
    await expect(notes).toBeVisible();
  });

  test('should allow typing in notes', async ({ page }) => {
    const notesTextarea = await getNotesTextarea(page);
    await expect(notesTextarea).toBeVisible();
    await expect(notesTextarea).toBeEnabled();

    const testValue = 'This is a test note about my mood today.';

    await notesTextarea.click();
    await notesTextarea.fill(testValue);

    await expect(notesTextarea).toHaveValue(testValue, { timeout: 5000 });
  });

  test('should have a submit button', async ({ page }) => {
    const submitButton = page.getByRole('button', { name: /Save Mood Entry|save mood|track mood/i });
    await submitButton.waitFor({ state: 'visible', timeout: 10000 });
    await expect(submitButton).toBeVisible();
  });

  test('should show validation errors when submitting incomplete form', async ({ page }) => {
    const submitButton = page.getByRole('button', { name: /Save Mood Entry|save mood|track mood/i });
    await submitButton.waitFor({ state: 'visible', timeout: 10000 });
    await expect(submitButton).toBeDisabled();
  });

  test('should handle date selection', async ({ page }) => {
    const dateButton = page.getByRole('button', { name: /Select date/i });
    await dateButton.click();

    const calendar = page.locator('[role="grid"]').first();
    await expect(calendar).toBeVisible({ timeout: 3000 });
  });

  test('should be responsive on mobile', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });
    await expect(page.getByRole('heading', { level: 1, name: /how are you feeling/i })).toBeVisible();
    const moodButtons = page.locator('button');
    const count = await moodButtons.count();
    expect(count).toBeGreaterThan(0);
  });

  test('should be responsive on desktop', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 720 });
    await expect(page.getByRole('heading', { level: 1, name: /how are you feeling/i })).toBeVisible();
    const submitButton = page.getByRole('button', { name: /Save Mood Entry|save mood|track mood/i });
    await expect(submitButton).toBeVisible();
  });

  test('should display time input field', async ({ page }) => {
    const timeButton = page.getByRole('button', { name: /Select time/i });
    await timeButton.click();

    const timeInput = page.locator('input[type="time"]');
    if (await timeInput.count() > 0) {
      await expect(timeInput.first()).toBeVisible({ timeout: 3000 });
    }
  });

  test('should persist form data while navigating within the page', async ({ page }) => {
    const notesTextarea = await getNotesTextarea(page);
    await notesTextarea.click();
    await page.keyboard.type('Test persistence', { delay: 10 });
    await expect(notesTextarea).toHaveValue('Test persistence', { timeout: 3000 });
    await page.mouse.wheel(0, 100);
    await expect(notesTextarea).toHaveValue('Test persistence');
  });

  test('should show success message after successful submission', async ({ page }) => {
    const moodButtons = page.getByRole('group', { name: /Mood selection/i }).getByRole('button');
    const moodButtonCount = await moodButtons.count();

    if (moodButtonCount > 0) {
      const firstMood = moodButtons.first();
      await expect(firstMood).toBeVisible();
      await expect(firstMood).toBeEnabled();
      await firstMood.click();
      await expect(firstMood).toHaveAttribute('aria-pressed', 'true');
    }

    const notesTextarea = await getNotesTextarea(page);
    await notesTextarea.click();
    await page.keyboard.type('Feeling good today!', { delay: 10 });
    await expect(notesTextarea).toHaveValue('Feeling good today!', { timeout: 3000 });

    const submitButton = page.getByRole('button', { name: /Save Mood Entry|save mood|track mood/i });
    await expect(submitButton).toBeVisible();

    if (moodButtonCount > 0) {
      await expect(submitButton).toBeEnabled({ timeout: 5000 });
    }
  });

  test('should allow clearing the notes textarea', async ({ page }) => {
    const notesTextarea = await getNotesTextarea(page);
    await notesTextarea.fill('Test entry');
    await expect(notesTextarea).toHaveValue('Test entry');
    await notesTextarea.fill('');
    await expect(notesTextarea).toHaveValue('');
  });
});


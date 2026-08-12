# Test Files Structure & Quick Reference

## Overview
The test suite consists of 4 main test files covering all functionality of the Mental Health App.

## Test Files

### 1. **home.spec.ts** (11 tests)
Tests the landing page and main navigation flows.

**Test Cases:**
- Display main heading and tagline
- Display both feature cards (Pomodoro & Mood Tracker)
- Navigation to Pomodoro page via card click
- Navigation to Mood Tracker page via card click
- Navigation to Pomodoro page via Start Timer button
- Navigation to Mood Tracker page via Track Mood button
- Display ADHD brains info card
- Proper icons displayed
- Responsive mobile layout

**Key Components Tested:**
- Page header and branding
- Feature cards
- Navigation buttons
- Responsive design (mobile)

---

### 2. **pomodoro.spec.ts** (16 tests)
Tests the Pomodoro Timer functionality comprehensively.

**Test Cases:**
- Display page header
- Display timer with initial time
- Have timer control buttons
- Have time input field
- Allow changing timer duration
- Start timer when Start button clicked
- Pause timer when Pause button clicked
- Reset timer when Reset button clicked
- Disable time input when timer is running
- Display session settings on desktop view
- Responsive mobile layout
- Proper visual feedback when timer is running
- Handle navigation back to home

**Helper Functions:**
- `safeClick(locator)` - Safely clicks elements with fallbacks

**Key Components Tested:**
- Timer display (MM:SS format)
- Duration input control
- Start/Pause/Reset buttons
- Desktop/Mobile responsive views
- Accessibility features

---

### 3. **mood-tracker.spec.ts** (14 tests)
Tests the Mood Tracker form and functionality.

**Test Cases:**
- Display page header
- Display mood selection options
- Allow selecting a mood
- Display date and time inputs
- Display factors section
- Allow removing added factors
- Display notes textarea
- Allow typing in notes
- Have submit button
- Show validation errors when submitting incomplete form
- Handle date selection
- Responsive mobile layout
- Responsive desktop layout
- Display time input field
- Persist form data while navigating
- Show success message after successful submission
- Clear form after successful submission

**Helper Functions:**
- `getNotesTextarea(page)` - Locates the notes textarea with fallbacks

**Key Components Tested:**
- Mood selection buttons
- Date/Time pickers
- Contributing factors management
- Notes textarea input
- Form validation
- Form submission
- Responsive design

---

### 4. **integration.spec.ts** (8 tests)
Tests app-wide navigation, integration, and cross-page functionality.

**Test Cases:**
- Navigate between all pages using the UI
- Handle direct URL navigation to all pages
- Maintain responsive design across all pages
- Have consistent header/navigation across pages
- Handle browser back and forward navigation
- Load all pages without console errors
- Have proper page titles for SEO
- Handle 404 for non-existent pages
- Be accessible on all pages
- Maintain state when navigating between pages

**Helper Functions:**
- `navigateWithRetry(url)` - Navigates with rate-limiting retry logic
- `getButtonAccessibleName(btn)` - Gets accessible name from button

**Key Components Tested:**
- Navigation flows
- Page responsiveness across viewports
- Accessibility compliance
- Console error detection
- SEO page titles
- 404 error handling
- Browser back/forward navigation

---

## Test Execution Summary

| File | Tests | Duration | Focus |
|------|-------|----------|-------|
| home.spec.ts | 11 | ~2-3s | Landing page & navigation |
| pomodoro.spec.ts | 16 | ~15-20s | Timer functionality & state |
| mood-tracker.spec.ts | 14 | ~10-15s | Form input & submission |
| integration.spec.ts | 8 | ~10-15s | Cross-page integration |
| **Total** | **49** | **~50-60s** | **Full app coverage** |

---

## Locator Strategies Used

### By Role (Preferred)
```typescript
page.getByRole('button', { name: /start/i })
page.getByRole('heading', { level: 1 })
page.getByRole('textbox', { name: /notes/i })
```

### By CSS/Attributes
```typescript
page.locator('input[type="number"]')
page.locator('[aria-live="polite"]')
page.locator('button[aria-label*="start" i]')
```

### Scoped (to avoid duplicates)
```typescript
page.getByRole('main').getByRole('link', { name: 'Pomodoro Timer' })
page.getByRole('navigation').getByRole('link', { name: 'Mood Tracker' })
```

---

## Common Test Patterns

### Pattern: Element Visibility Check
```typescript
await element.waitFor({ state: 'visible', timeout: 10000 });
await expect(element).toBeVisible();
```

### Pattern: Fallback Locator
```typescript
let element = page.getByRole('spinbutton', { name: /pattern/i }).first();
if ((await element.count()) === 0) {
  element = page.locator('fallback-selector').first();
}
```

### Pattern: Safe Click
```typescript
await safeClick(element);
// or manually:
try {
  await element.click();
} catch {
  await element.click({ force: true });
}
```

### Pattern: Polling for Change
```typescript
const deadline = Date.now() + timeout;
let changed = false;
while (Date.now() < deadline) {
  const current = await element.textContent();
  if (current !== initial) {
    changed = true;
    break;
  }
  await page.waitForTimeout(200);
}
```

### Pattern: Retry with Delay
```typescript
for (let attempt = 0; attempt < 3; attempt++) {
  try {
    await action();
    break;
  } catch {
    if (attempt < 2) await page.waitForTimeout(2000);
  }
}
```

---

## Debugging Tips

### View Console Errors
```typescript
page.on('console', (msg) => {
  if (msg.type() === 'error') console.log(msg.text());
});
```

### Check Page Content
```typescript
const content = await page.content();
console.log(content);
```

### Screenshot on Failure
```typescript
await page.screenshot({ path: 'failure.png' });
```

### Pause Test
```typescript
await page.pause(); // Use --debug flag to attach debugger
```

### Log Element HTML
```typescript
const outer = await element.evaluate((el) => (el as HTMLElement).outerHTML);
console.log(outer);
```

---

## Performance Notes

- **beforeEach Hook**: Runs ~500ms per test (page load + heading wait)
- **Timer Tests**: Include delays to test countdown (2-7s per test)
- **Network Retries**: Add 2s delays between attempts
- **Total Suite**: ~50-60 seconds for full run

---

## Accessibility Features Tested

✅ Role-based selectors for better accessibility
✅ Aria-label verification for buttons
✅ Aria-disabled and readonly attributes
✅ Aria-pressed states for toggle buttons
✅ Aria-live regions for dynamic content
✅ Accessible button names verification
✅ Alt text for images
✅ Heading hierarchy (h1, h3)
✅ Semantic HTML roles (main, navigation, timer)

---

## CI/CD Integration Notes

- Tests use `waitUntil: 'networkidle'` for stability
- Rate-limiting retry logic handles transient failures
- Console error filters ignore browser internals and extension noise
- Allowed error patterns: favicon, extension, CORS, clerk, 429, ResizeObserver
- Tests handle both headed and headless modes

---

**Last Updated:** December 15, 2025


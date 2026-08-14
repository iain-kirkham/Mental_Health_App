import type { EnergyRating } from '@/types'

const SCORE_EMOJIS = ['😢', '😕', '😐', '😊', '🎉']

export function getScoreEmoji(score: number): string {
  return SCORE_EMOJIS[score - 1] || '😐'
}

export const ENERGY_RATING_OPTIONS: {
  value: EnergyRating
  label: string
  icon: string
  colorClass: string
  badgeClass: string
  ariaLabel: string
}[] = [
  {
    value: 'ENERGIZING',
    label: 'Energizing',
    icon: '⚡',
    colorClass: 'bg-emerald-50 hover:bg-emerald-100 border-emerald-300 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300',
    badgeClass: 'border-transparent bg-emerald-50 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300',
    ariaLabel: 'Toggle energizing rating',
  },
  {
    value: 'DRAINING',
    label: 'Draining',
    icon: '🪫',
    colorClass: 'bg-amber-50 hover:bg-amber-100 border-amber-300 text-amber-700 dark:bg-amber-950 dark:text-amber-300',
    badgeClass: 'border-transparent bg-amber-50 text-amber-700 dark:bg-amber-950 dark:text-amber-300',
    ariaLabel: 'Toggle draining rating',
  },
]

export function getEnergyRatingOption(energyRating: EnergyRating | null | undefined) {
  return ENERGY_RATING_OPTIONS.find((option) => option.value === energyRating) ?? null
}

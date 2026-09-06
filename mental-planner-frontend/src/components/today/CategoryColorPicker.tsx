'use client'

import { Check } from 'lucide-react'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { cn } from '@/lib/utils'
import { CHANNEL_COLORS, normalizeCategoryKey } from '@/lib/channel-color'
import { useChannelColor } from '@/hooks/useChannelColor'
import { useCategoryColorStore } from '@/store/categoryColorStore'

interface CategoryColorPickerProps {
  category: string
}

/** A single colored-dot trigger that pops out an 8-swatch grid for manually assigning a
 * category's color, overriding the deterministic hash-based default. The override is keyed by
 * category name and reused everywhere that name appears (pills, card tints, timeline blocks) -
 * see useChannelColor. Kept as a compact popover rather than an inline row of swatches, which
 * blew out the metadata bar's width on both desktop and mobile. */
export default function CategoryColorPicker({ category }: CategoryColorPickerProps) {
  const setCategoryColorIndex = useCategoryColorStore((state) => state.setCategoryColorIndex)
  const channelColor = useChannelColor(category)

  const key = normalizeCategoryKey(category)
  if (!key) return null

  return (
    <Popover>
      <PopoverTrigger asChild>
        <button
          type="button"
          className="flex h-5 w-5 shrink-0 items-center justify-center rounded-full hover:ring-2 hover:ring-muted-foreground/30"
          aria-label={`Category color: ${channelColor.name}`}
        >
          <span className={cn('h-3 w-3 rounded-full', channelColor.dot)} />
        </button>
      </PopoverTrigger>
      <PopoverContent align="start" className="w-auto p-2">
        <div className="flex items-center gap-1" role="group" aria-label="Category color">
          {CHANNEL_COLORS.map((entry, index) => {
            const isActiveOverride = channelColor.isOverride && channelColor.index === index
            return (
              <button
                key={entry.name}
                type="button"
                onClick={() => setCategoryColorIndex(key, index)}
                className={cn(
                  'flex h-6 w-6 shrink-0 items-center justify-center rounded-full transition-transform hover:scale-110',
                  entry.dot,
                  isActiveOverride && 'ring-2 ring-foreground ring-offset-1 ring-offset-popover'
                )}
                aria-label={entry.name}
                aria-pressed={isActiveOverride}
              >
                {isActiveOverride && <Check className="h-3 w-3 text-background" />}
              </button>
            )
          })}
          {channelColor.isOverride && (
            <button
              type="button"
              onClick={() => setCategoryColorIndex(key, null)}
              className="ml-1 rounded-sm px-1.5 py-1 text-[10px] text-muted-foreground hover:bg-muted/50 hover:text-foreground"
            >
              Auto
            </button>
          )}
        </div>
      </PopoverContent>
    </Popover>
  )
}

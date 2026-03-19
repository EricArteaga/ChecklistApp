import React from 'react'

/**
 * ═══════════════════════════════════════════════════════════════════
 * SKELETON - Base skeleton component for loading states
 * ═══════════════════════════════════════════════════════════════════
 *
 * Purpose:
 * • Provides visual placeholder during content loading
 * • Reduces perceived loading time by showing structure immediately
 * • Improves UX with progressive disclosure of content
 *
 * Variants:
 * • default - Standard gray box
 * • text - Single line of text
 * • circle - Circular avatar/icon placeholder
 * • progress - Progress bar placeholder
 *
 * @example
 * <Skeleton className="h-4 w-32" variant="text" />
 * <Skeleton className="h-12 w-12 rounded-full" variant="circle" />
 */
const Skeleton = ({ className = '', variant = 'default' }) => {
  const variantClasses = {
    default: '',
    text: 'rounded',
    circle: 'rounded-full',
    progress: 'rounded-full',
  }

  const baseClasses = 'animate-pulse bg-muted'
  const variantClass = variantClasses[variant] || ''

  return (
    <div
      className={`${baseClasses} ${variantClass} ${className}`.trim()}
      aria-hidden="true"
    />
  )
}

export default Skeleton

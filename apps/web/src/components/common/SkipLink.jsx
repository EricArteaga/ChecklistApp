import React from 'react'

/**
 * ═══════════════════════════════════════════════════════════════════
 * SKIP LINK - Accessibility skip link for keyboard navigation
 * ═══════════════════════════════════════════════════════════════════
 *
 * Purpose:
 * • Allows keyboard users to skip repetitive navigation
 * • WCAG 2.1 AA compliance requirement
 * • Hidden by default, visible on focus
 * • Improves experience for screen reader users
 *
 * Usage:
 * Place at the top of the app, before main navigation
 *
 * @example
 * <SkipLink href="#main-content">Saltar al contenido principal</SkipLink>
 * <main id="main-content">...</main>
 */
const SkipLink = ({ href = '#main-content', children = 'Saltar al contenido principal' }) => {
  return (
    <a
      href={href}
      className="
        sr-only focus:not-sr-only
        focus:absolute focus:top-4 focus:left-4 focus:z-50
        focus:px-4 focus:py-2 focus:bg-primary focus:text-primary-foreground
        focus:rounded-lg focus:shadow-medium
        focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2
        transition-all duration-200
        font-medium text-sm
      "
    >
      {children}
    </a>
  )
}

export default SkipLink

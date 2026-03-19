import { useEffect, useCallback } from 'react'

/**
 * ═══════════════════════════════════════════════════════════════════
 * USE KEYBOARD SHORTCUTS - Global keyboard shortcuts handler
 * ═══════════════════════════════════════════════════════════════════
 *
 * Purpose:
 * • Add keyboard shortcuts for power users
 * • Improve efficiency for common actions
 * • Reduce mouse dependency
 * • Standard shortcuts (Ctrl+N, Ctrl+F, Escape)
 *
 * Supported Modifiers:
 * • ctrlKey / metaKey (Cmd on Mac)
 * • shiftKey
 * • altKey
 *
 * @example
 * useKeyboardShortcuts({
 *   'n': () => inputRef.current?.focus(),  // Ctrl+N: New task
 *   'f': () => filterRef.current?.focus(), // Ctrl+F: Filter
 *   'Escape': () => setExpanded(null),      // Escape: Close
 * })
 */
export const useKeyboardShortcuts = (shortcuts, options = {}) => {
  const {
    enabled = true,
    preventDefault = true,
    requireCtrl = true,
  } = options

  const handleKeyDown = useCallback((e) => {
    if (!enabled) return

    // Check if required modifier is pressed
    if (requireCtrl && !e.ctrlKey && !e.metaKey) return
    if (requireCtrl && e.shiftKey) return // Don't trigger if Ctrl+Shift

    // Get the key (handle both upper and lower case)
    const key = e.key.toLowerCase()

    // Find matching shortcut
    const shortcutHandler = shortcuts[key] || shortcuts[e.key]

    if (shortcutHandler) {
      if (preventDefault) {
        e.preventDefault()
        e.stopPropagation()
      }

      shortcutHandler(e)
    }
  }, [shortcuts, enabled, preventDefault, requireCtrl])

  useEffect(() => {
    window.addEventListener('keydown', handleKeyDown)

    return () => {
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [handleKeyDown])
}

export default useKeyboardShortcuts

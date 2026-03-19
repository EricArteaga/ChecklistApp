import { useState, useEffect } from 'react'

const THEME_KEY = 'checklistapp-theme'
const themes = ['light', 'dark', 'auto']

/**
 * ═══════════════════════════════════════════════════════════════════
 * USE THEME - Custom hook for persistent theme management
 * ═══════════════════════════════════════════════════════════════════
 *
 * Features:
 * • Persists theme to localStorage
 * • Supports 'auto' mode that follows system preference
 * • Respects system preference (prefers-color-scheme) on first visit
 * • Prevents FOUC (Flash of Unstyled Content) by applying theme early
 * • Smooth transitions between themes
 *
 * @returns {Object} { theme, effectiveTheme, toggleTheme, setTheme }
 */
export function useTheme() {
  const [theme, setThemeState] = useState(() => {
    // 1. Check localStorage for saved theme preference
    const savedTheme = localStorage.getItem(THEME_KEY)
    if (savedTheme && themes.includes(savedTheme)) {
      return savedTheme
    }

    // 2. Fall back to 'auto' (system preference) if no saved preference
    return 'auto'
  })

  // Listen for system theme changes when in 'auto' mode
  useEffect(() => {
    if (theme !== 'auto') return

    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')

    const handleChange = () => {
      // Force re-render to update effective theme
      setThemeState('auto')
    }

    // Modern browsers
    if (mediaQuery.addEventListener) {
      mediaQuery.addEventListener('change', handleChange)
      return () => mediaQuery.removeEventListener('change', handleChange)
    }
    // Legacy browsers
    else if (mediaQuery.addListener) {
      mediaQuery.addListener(handleChange)
      return () => mediaQuery.removeListener(handleChange)
    }
  }, [theme])

  // Apply theme to document and localStorage whenever it changes
  useEffect(() => {
    const root = document.documentElement

    // Determine effective theme (auto uses system preference)
    const effectiveTheme = theme === 'auto'
      ? (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light')
      : theme

    // Remove both classes first
    root.classList.remove('light', 'dark')

    // Add the effective theme class
    root.classList.add(effectiveTheme)

    // Persist to localStorage
    localStorage.setItem(THEME_KEY, theme)

    // Update meta theme-color for browser UI
    const metaThemeColor = document.querySelector('meta[name="theme-color"]')
    if (metaThemeColor) {
      metaThemeColor.setAttribute('content', effectiveTheme === 'dark' ? '#0f172a' : '#ffffff')
    }
  }, [theme])

  // Toggle between light, dark, and auto themes
  const toggleTheme = () => {
    setThemeState(prevTheme => {
      if (prevTheme === 'light') return 'dark'
      if (prevTheme === 'dark') return 'auto'
      return 'light'
    })
  }

  // Get the effective theme (resolves 'auto' to actual theme)
  const effectiveTheme = theme === 'auto'
    ? (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light')
    : theme

  // Set a specific theme (for manual theme selection)
  const setTheme = (newTheme) => {
    if (themes.includes(newTheme)) {
      setThemeState(newTheme)
    } else {
      console.warn(`Invalid theme: ${newTheme}. Must be one of:`, themes)
    }
  }

  return {
    theme,         // User's preference: 'light', 'dark', or 'auto'
    effectiveTheme, // Actual theme being used: 'light' or 'dark'
    toggleTheme,
    setTheme,
    themes,
  }
}

/**
 * ═══════════════════════════════════════════════════════════════════
 * HEATMAP SERVICE - Real activity data for task completion tracking
 * ═══════════════════════════════════════════════════════════════════
 *
 * Features:
 * • Fetches real task completion data from backend
 * • Supports both authenticated and anonymous users
 * • Caches results in localStorage (1h TTL)
 * • Falls back to empty data on error
 * • Processes activity by date for heatmap visualization
 */

import authService from './authService'
import taskService from './taskService'
import localStorageService from './localStorageService'

const CACHE_KEY = 'heatmap-activity-data'
const CACHE_TTL = 60 * 60 * 1000 // 1 hour in milliseconds

/**
 * Get cached activity data if valid
 * @returns {Object|null} Cached data or null if expired/invalid
 */
function getCachedData() {
  try {
    const cached = localStorage.getItem(CACHE_KEY)
    if (!cached) return null

    const { data, timestamp } = JSON.parse(cached)
    const now = Date.now()

    // Check if cache is still valid (within TTL)
    if (now - timestamp < CACHE_TTL) {
      return data
    }

    // Cache expired, remove it
    localStorage.removeItem(CACHE_KEY)
    return null
  } catch (error) {
    console.error('Error reading cache:', error)
    return null
  }
}

/**
 * Save activity data to cache with timestamp
 * @param {Object} data - Activity data to cache
 */
function setCachedData(data) {
  try {
    const cacheEntry = {
      data,
      timestamp: Date.now()
    }
    localStorage.setItem(CACHE_KEY, JSON.stringify(cacheEntry))
  } catch (error) {
    console.error('Error writing cache:', error)
  }
}

/**
 * Process tasks to generate activity data for heatmap
 * @param {Array} tasks - Array of tasks with completion info
 * @param {number} days - Number of days to include (default: 365)
 * @returns {Array} Activity data with date, level, and count
 */
function processActivityData(tasks, days = 365) {
  const today = new Date()
  const activityMap = new Map()

  // Initialize all days with 0 activity
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(today)
    date.setDate(date.getDate() - i)
    const dateKey = date.toISOString().split('T')[0] // YYYY-MM-DD format
    activityMap.set(dateKey, { date: dateKey, count: 0, level: 0 })
  }

  // Process tasks to count completions per day
  tasks.forEach(task => {
    // Use fechaRealizacion (completion date) if task is completed
    if (task.completada && task.fechaRealizacion) {
      const completionDate = new Date(task.fechaRealizacion)
      const dateKey = completionDate.toISOString().split('T')[0]

      if (activityMap.has(dateKey)) {
        const current = activityMap.get(dateKey)
        current.count++
      }
    }
  })

  // Also check subitems for activity
  tasks.forEach(task => {
    if (task.subitems && Array.isArray(task.subitems)) {
      task.subitems.forEach(subitem => {
        if (subitem.checked && subitem.completedAt) {
          const completionDate = new Date(subitem.completedAt)
          const dateKey = completionDate.toISOString().split('T')[0]

          if (activityMap.has(dateKey)) {
            const current = activityMap.get(dateKey)
            current.count++
          }
        }
      })
    }
  })

  // Convert map to array and calculate activity levels
  const activityData = Array.from(activityMap.values()).map(item => {
    const maxCount = Math.max(...Array.from(activityMap.values()).map(d => d.count), 1)
    const normalizedCount = item.count / maxCount

    // Calculate level (0-4) based on count distribution
    let level
    if (item.count === 0) {
      level = 0
    } else if (normalizedCount <= 0.25) {
      level = 1
    } else if (normalizedCount <= 0.5) {
      level = 2
    } else if (normalizedCount <= 0.75) {
      level = 3
    } else {
      level = 4
    }

    return {
      date: item.date,
      dateObj: new Date(item.date),
      level,
      count: item.count
    }
  })

  return activityData
}

/**
 * Fetch activity data for heatmap
 * @param {number} days - Number of days to include (default: 365)
 * @returns {Promise<Array>} Activity data array
 */
export async function getHeatmapData(days = 365) {
  // Try to get cached data first
  const cached = getCachedData()
  if (cached) {
    return cached
  }

  try {
    let tasks = []

    if (authService.isAuthenticated()) {
      // Authenticated user: fetch from backend
      const user = await authService.getMe()
      tasks = await taskService.getTasksByUser(user.id)
    } else {
      // Anonymous user: fetch from localStorage
      tasks = localStorageService.getTasks()
    }

    // Process tasks to generate activity data
    const activityData = processActivityData(tasks, days)

    // Cache the results
    setCachedData(activityData)

    return activityData
  } catch (error) {
    console.error('Error fetching heatmap data:', error)
    // Return empty array on error (fallback to empty state)
    return []
  }
}

/**
 * Clear cached heatmap data (call after task completion)
 */
export function clearHeatmapCache() {
  localStorage.removeItem(CACHE_KEY)
}

/**
 * Get statistics from activity data
 * @param {Array} activityData - Activity data array
 * @returns {Object} Statistics object
 */
export function getHeatmapStats(activityData) {
  const totalDays = activityData.length
  const activeDays = activityData.filter(d => d.level > 0).length
  const totalTasks = activityData.reduce((sum, d) => sum + d.count, 0)

  // Calculate current streak
  let currentStreak = 0
  const today = new Date()

  for (let i = 0; i < activityData.length; i++) {
    const item = activityData[i]
    const diffTime = Math.abs(today - item.dateObj)
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

    if (diffDays === i && item.level > 0) {
      currentStreak++
    } else if (diffDays === i && item.level === 0) {
      break
    }
  }

  return {
    totalDays,
    activeDays,
    totalTasks,
    currentStreak,
    activityRate: Math.round((activeDays / totalDays) * 100)
  }
}

export default {
  getHeatmapData,
  clearHeatmapCache,
  getHeatmapStats
}

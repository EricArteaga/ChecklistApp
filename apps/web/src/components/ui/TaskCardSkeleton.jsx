import React from 'react'
import { Card, CardHeader, CardContent, Badge } from '@checklist/ui'
import Skeleton from './Skeleton'

/**
 * ═══════════════════════════════════════════════════════════════════
 * TASK CARD SKELETON - Loading placeholder for task/checklist cards
 * ═══════════════════════════════════════════════════════════════════
 *
 * Purpose:
 * • Shows the structure of task cards during loading
 * • Matches the exact layout of real task cards
 * • Provides visual continuity when loading data
 *
 * Structure matches TaskCard:
 * • Color dot + Title + Badge
 * • Description text
 * • Progress bar with percentage
 * • Action buttons (expand, delete)
 *
 * @example
 * <TaskCardSkeleton />
 * <TaskCardSkeleton showExpanded />
 */
const TaskCardSkeleton = ({ showExpanded = false, className = '', style }) => {
  return (
    <Card
      className={`card-elevated ${className}`}
      style={style}
      aria-hidden="true"
    >
      <CardHeader>
        {/* Header section: Color dot + Title + Badge + Actions */}
        <div className="flex items-start justify-between gap-4">
          <div className="flex-1 min-w-0 space-y-3">
            {/* Color dot + Title + Badge row */}
            <div className="flex items-center gap-2">
              {/* Color dot placeholder */}
              <Skeleton className="w-3 h-3 rounded-full" variant="circle" />

              {/* Title placeholder */}
              <Skeleton className="h-6 w-48" variant="text" />

              {/* Badge placeholder */}
              <Skeleton className="h-5 w-16 rounded-full" />
            </div>

            {/* Description placeholder */}
            <Skeleton className="h-4 w-64 max-w-full" variant="text" />
          </div>

          {/* Action buttons placeholder */}
          <div className="flex items-center gap-2 shrink-0">
            {/* Expand/collapse button */}
            <Skeleton className="w-8 h-8 rounded-lg" />

            {/* Delete button */}
            <Skeleton className="w-8 h-8 rounded-lg" />
          </div>
        </div>

        {/* Progress bar section */}
        <div className="mt-5 space-y-2">
          {/* Progress label and percentage */}
          <div className="flex items-center justify-between">
            <Skeleton className="h-4 w-20" variant="text" />
            <Skeleton className="h-4 w-8" variant="text" />
          </div>

          {/* Progress bar */}
          <div className="w-full h-3 bg-muted rounded-full overflow-hidden">
            <Skeleton
              className="h-full w-3/4 rounded-full"
              variant="progress"
            />
          </div>
        </div>
      </CardHeader>

      {/* Expanded content placeholder (optional) */}
      {showExpanded && (
        <CardContent className="border-t bg-muted/10">
          <div className="space-y-3">
            {/* Input placeholder */}
            <div className="flex gap-2">
              <Skeleton className="flex-1 h-10 rounded-lg" />
              <Skeleton className="w-10 h-10 rounded-lg" />
            </div>

            {/* Item placeholders */}
            {[1, 2, 3].map((i) => (
              <div key={i} className="flex items-center gap-3 p-4 rounded-xl border">
                {/* Checkbox placeholder */}
                <Skeleton className="w-6 h-6 rounded-lg shrink-0" variant="circle" />

                {/* Text placeholder */}
                <Skeleton className="flex-1 h-4 w-full max-w-md" variant="text" />

                {/* Delete button placeholder */}
                <Skeleton className="w-8 h-8 rounded-lg shrink-0" />
              </div>
            ))}
          </div>
        </CardContent>
      )}
    </Card>
  )
}

export default TaskCardSkeleton

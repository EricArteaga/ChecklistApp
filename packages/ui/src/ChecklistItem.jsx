import React, { useState } from 'react'

export default function ChecklistItem({
  item,
  onToggle,
  onDelete,
  onEdit,
  readOnly = false,
}) {
  const [isEditing, setIsEditing] = useState(false)
  const [editValue, setEditValue] = useState(item.description || '')

  const handleToggle = () => {
    if (!readOnly && onToggle) {
      onToggle(item.id)
    }
  }

  const handleDelete = () => {
    if (!readOnly && onDelete) {
      onDelete(item.id)
    }
  }

  const handleEdit = () => {
    if (!readOnly && onEdit) {
      setIsEditing(true)
    }
  }

  const handleSaveEdit = () => {
    if (onEdit && editValue.trim()) {
      onEdit(item.id, editValue.trim())
      setIsEditing(false)
    }
  }

  const handleCancelEdit = () => {
    setEditValue(item.description || '')
    setIsEditing(false)
  }

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleSaveEdit()
    } else if (e.key === 'Escape') {
      handleCancelEdit()
    }
  }

  return (
    <div
      className={`
        group flex items-center gap-3 rounded-lg border p-4
        transition-all duration-200
        ${item.checked
          ? 'bg-muted/50 border-muted-foreground/20'
          : 'bg-card border-border hover:border-primary/50 hover:shadow-sm'
        }
        ${readOnly ? 'cursor-default' : 'cursor-pointer'}
      `}
      onClick={handleToggle}
      role="listitem"
      aria-label={item.description}
    >
      {/* Checkbox */}
      <div className="relative flex items-center">
        <input
          type="checkbox"
          checked={item.checked}
          onChange={handleToggle}
          disabled={readOnly}
          className={`
            h-5 w-5 rounded border-2 transition-all duration-200
            ${
              item.checked
                ? 'border-primary bg-primary text-primary-foreground'
                : 'border-input bg-background'
            }
            ${readOnly ? 'cursor-not-allowed opacity-50' : 'cursor-pointer'}
            focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2
          `}
          aria-label={`Mark "${item.description || 'item'}" as ${item.checked ? 'incomplete' : 'complete'}`}
          onClick={(e) => e.stopPropagation()}
        />
        {item.checked && (
          <svg
            className="absolute left-0.5 h-4 w-4 text-primary-foreground pointer-events-none"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={3}
              d="M5 13l4 4L19 7"
            />
          </svg>
        )}
      </div>

      {/* Content */}
      <div className="flex-1 min-w-0">
        {isEditing ? (
          <div
            className="flex items-center gap-2"
            onClick={(e) => e.stopPropagation()}
          >
            <input
              type="text"
              value={editValue}
              onChange={(e) => setEditValue(e.target.value)}
              onKeyDown={handleKeyPress}
              autoFocus
              className="flex-1 px-2 py-1 text-sm border border-input rounded-md bg-background focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2"
              aria-label="Edit item description"
            />
            <button
              onClick={handleSaveEdit}
              className="p-1 text-primary hover:bg-primary/10 rounded transition-colors"
              aria-label="Save changes"
            >
              <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </button>
            <button
              onClick={handleCancelEdit}
              className="p-1 text-destructive hover:bg-destructive/10 rounded transition-colors"
              aria-label="Cancel editing"
            >
              <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        ) : (
          <p
            className={`
              text-sm transition-all duration-200
              ${item.checked
                ? 'text-muted-foreground line-through'
                : 'text-foreground'
              }
            `}
          >
            {item.description || <span className="italic text-muted-foreground">Sin descripción</span>}
          </p>
        )}
      </div>

      {/* Actions */}
      {!readOnly && !isEditing && (
        <div
          className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity duration-200"
          onClick={(e) => e.stopPropagation()}
        >
          <button
            onClick={handleEdit}
            className="p-2 text-muted-foreground hover:text-primary hover:bg-primary/10 rounded transition-all duration-200"
            aria-label={`Edit "${item.description || 'item'}"`}
            title="Edit"
          >
            <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
              />
            </svg>
          </button>
          <button
            onClick={handleDelete}
            className="p-2 text-muted-foreground hover:text-destructive hover:bg-destructive/10 rounded transition-all duration-200"
            aria-label={`Delete "${item.description || 'item'}"`}
            title="Delete"
          >
            <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
              />
            </svg>
          </button>
        </div>
      )}
    </div>
  )
}

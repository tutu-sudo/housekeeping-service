const MASKED_TEXT_PATTERNS = ['[已屏蔽]', '已屏蔽']

const readMaskFlag = (value) => {
  if (value === true) return true
  if (value === 1 || value === '1') return true
  if (typeof value === 'string') {
    const normalized = value.trim().toLowerCase()
    return normalized === 'masked' || normalized === 'hidden' || normalized === 'blocked'
  }
  return false
}

export function isReviewMasked(review) {
  if (!review || typeof review !== 'object') return false

  const explicitMaskFields = [
    'isMasked',
    'masked',
    'maskStatus',
    'mask_status',
    'reviewStatus',
    'review_status'
  ]

  const hasExplicitField = explicitMaskFields.some((field) => review[field] !== undefined && review[field] !== null)
  if (hasExplicitField) {
    return (
      readMaskFlag(review.isMasked) ||
      readMaskFlag(review.masked) ||
      readMaskFlag(review.maskStatus) ||
      readMaskFlag(review.mask_status) ||
      readMaskFlag(review.reviewStatus) ||
      readMaskFlag(review.review_status)
    )
  }

  const content =
    review.reviewContent ??
    review.content ??
    review.review_content ??
    ''
  const text = String(content).trim()
  if (!text) return false
  return MASKED_TEXT_PATTERNS.some((pattern) => text.includes(pattern))
}

export function normalizeVisibleReview(review) {
  return isReviewMasked(review) ? null : review
}

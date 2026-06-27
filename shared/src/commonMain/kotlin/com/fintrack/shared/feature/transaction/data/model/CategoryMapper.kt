package com.fintrack.shared.feature.transaction.data.model

import com.fintrack.shared.feature.transaction.domain.model.Category

fun CategoryDto.toDomain(): Category = Category(
    id = id,
    name = name,
    isExpense = isExpense,
    iconName = iconName,
    isDefault = isDefault
)

package com.fintrack.shared.feature.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.uikit.LocalUIViewController
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleCancel
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertActionStyleDestructive
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert

@Composable
actual fun ConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String,
    isDestructive: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val viewController = LocalUIViewController.current

    LaunchedEffect(Unit) {
        val alertController = UIAlertController.alertControllerWithTitle(
            title = title,
            message = message,
            preferredStyle = UIAlertControllerStyleAlert
        )

        alertController.addAction(
            UIAlertAction.actionWithTitle(
                title = confirmLabel,
                style = if (isDestructive) UIAlertActionStyleDestructive else UIAlertActionStyleDefault,
                handler = {
                    onConfirm()
                    onDismiss()
                }
            )
        )

        alertController.addAction(
            UIAlertAction.actionWithTitle(
                title = cancelLabel,
                style = UIAlertActionStyleCancel,
                handler = {
                    onDismiss()
                }
            )
        )

        viewController.presentViewController(alertController, animated = true, completion = null)
    }
}

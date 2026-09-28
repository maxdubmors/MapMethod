package dev.stekl0.mapmethod.core.data.di

import javax.inject.Qualifier

/** The scope of work that lives as long as the app, such as keeping Map progress read. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class ApplicationScope

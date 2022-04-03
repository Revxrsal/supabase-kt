package io.supabase.postgrest.builder

enum class Returning {
    MINIMAL,
    REPRESENTATION;

    override fun toString(): String {
        return name.lowercase()
    }
}

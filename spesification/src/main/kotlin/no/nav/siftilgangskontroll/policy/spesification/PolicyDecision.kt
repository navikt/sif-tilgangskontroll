package no.nav.siftilgangskontroll.policy.spesification

enum class PolicyDecision {
    PERMIT {
        override infix fun and(other: PolicyDecision): PolicyDecision = other
        override infix fun or(other: PolicyDecision): PolicyDecision = PERMIT
        override fun not(): PolicyDecision = DENY
    },

    DENY {
        override infix fun and(other: PolicyDecision): PolicyDecision = DENY
        override infix fun or(other: PolicyDecision): PolicyDecision = other
        override fun not(): PolicyDecision = NOT_APPLICABLE
    },

    NOT_APPLICABLE {
        override infix fun and(other: PolicyDecision): PolicyDecision = if (other == PERMIT) NOT_APPLICABLE else other
        override infix fun or(other: PolicyDecision): PolicyDecision = if (other == DENY) DENY else other
        override fun not(): PolicyDecision = NOT_APPLICABLE
    };

    abstract infix fun and(other: PolicyDecision): PolicyDecision
    abstract infix fun or(other: PolicyDecision): PolicyDecision
    abstract fun not(): PolicyDecision
}

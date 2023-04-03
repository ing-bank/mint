package org.mint.lib.query

import org.mint.lib.Query
import org.mint.lib.SUTState

// Query for a specific property such as app id, version, ...
class PropertyQuery<S : SUTState<S>> : Query<S>

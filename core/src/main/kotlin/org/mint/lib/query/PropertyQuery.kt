package com.ing.mint.lib.query

import com.ing.mint.lib.Query
import com.ing.mint.lib.SUTState

// Query for a specific property such as app id, version, ...
class PropertyQuery<S : SUTState<S>> : Query<S>

/*
 * Copyright 2010-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.jet.lang.resolve;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.jet.lang.diagnostics.Diagnostic;
import org.jetbrains.jet.util.slicedmap.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DelegatingBindingTrace implements BindingTrace {
    @SuppressWarnings("ConstantConditions")
    private final MutableSlicedMap map = BindingTraceContext.TRACK_REWRITES ? new TrackingSlicedMap(BindingTraceContext.TRACK_WITH_STACK_TRACES) : SlicedMapImpl.create();

    private final BindingContext parentContext;
    private final List<Diagnostic> diagnostics = Lists.newArrayList();
    private final String name;

    private final BindingContext bindingContext = new BindingContext() {
        @NotNull
        @Override
        public Diagnostics getDiagnostics() {
            ArrayList<Diagnostic> mergedDiagnostics = new ArrayList<Diagnostic>(diagnostics);
            mergedDiagnostics.addAll(parentContext.getDiagnostics().noSuppression().all());
            return new DiagnosticsWithSuppression(this, mergedDiagnostics);
        }

        @Override
        public <K, V> V get(ReadOnlySlice<K, V> slice, K key) {
            return DelegatingBindingTrace.this.get(slice, key);
        }

        @NotNull
        @Override
        public <K, V> Collection<K> getKeys(WritableSlice<K, V> slice) {
            return DelegatingBindingTrace.this.getKeys(slice);

        }

        @NotNull
        @TestOnly
        @Override
        public <K, V> ImmutableMap<K, V> getSliceContents(@NotNull ReadOnlySlice<K, V> slice) {
            ImmutableMap<K, V> parentContents = parentContext.getSliceContents(slice);
            ImmutableMap<K, V> currentContents = map.getSliceContents(slice);
            return ImmutableMap.<K, V>builder().putAll(parentContents).putAll(currentContents).build();
        }
    };

    public DelegatingBindingTrace(BindingContext parentContext, String debugName) {
        this.parentContext = parentContext;
        this.name = debugName;
    }

    public DelegatingBindingTrace(BindingContext parentContext, String debugName, @Nullable Object resolutionSubjectForMessage) {
        this(parentContext, AnalyzingUtils.formDebugNameForBindingTrace(debugName, resolutionSubjectForMessage));
    }

    @Override
    @NotNull
    public BindingContext getBindingContext() {
        return bindingContext;
    }

    @Override
    public <K, V> void record(WritableSlice<K, V> slice, K key, V value) {
        map.put(slice, key, value);
    }

    @Override
    public <K> void record(WritableSlice<K, Boolean> slice, K key) {
        record(slice, key, true);
    }

    @Override
    public <K, V> V get(ReadOnlySlice<K, V> slice, K key) {
        V value = map.get(slice, key);
        if (slice instanceof Slices.SetSlice) {
            assert value != null;
            if (value.equals(true)) return value;
        }
        else if (value != null) {
            return value;
        }

        return parentContext.get(slice, key);
    }

    @NotNull
    @Override
    public <K, V> Collection<K> getKeys(WritableSlice<K, V> slice) {
        Collection<K> keys = map.getKeys(slice);
        Collection<K> fromParent = parentContext.getKeys(slice);
        if (keys.isEmpty()) return fromParent;
        if (fromParent.isEmpty()) return keys;

        List<K> result = Lists.newArrayList(keys);
        result.addAll(fromParent);
        return result;
    }

    public void addAllMyDataTo(@NotNull BindingTrace trace) {
        addAllMyDataTo(trace, null, true);
    }

    public void moveAllMyDataTo(@NotNull BindingTrace trace) {
        addAllMyDataTo(trace, null, true);
        clear();
    }

    public void addAllMyDataTo(@NotNull BindingTrace trace, @Nullable TraceEntryFilter filter, boolean commitDiagnostics) {
        for (Map.Entry<SlicedMapKey<?, ?>, ?> entry : map) {
            SlicedMapKey slicedMapKey = entry.getKey();

            WritableSlice slice = slicedMapKey.getSlice();
            Object key = slicedMapKey.getKey();
            Object value = entry.getValue();

            if (filter == null || filter.accept(slice, key)) {
                //noinspection unchecked
                trace.record(slice, key, value);
            }
        }

        if (!commitDiagnostics) return;

        for (Diagnostic diagnostic : diagnostics) {
            if (filter == null || filter.accept(null, diagnostic.getPsiElement())) {
                trace.report(diagnostic);
            }
        }
    }

    public void clear() {
        map.clear();
        diagnostics.clear();
    }

    @Override
    public void report(@NotNull Diagnostic diagnostic) {
        diagnostics.add(diagnostic);
    }

    @Override
    public String toString() {
        return name;
    }
}

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.sql.planner;

import io.trino.metadata.Metadata;
import io.trino.metadata.ResolvedFunction;
import io.trino.spi.type.Type;
import io.trino.spi.type.TypeSignature;
import io.trino.sql.analyzer.TypeSignatureProvider;
import io.trino.sql.tree.Expression;
import io.trino.sql.tree.FunctionCall;
import io.trino.sql.tree.NodeLocation;
import io.trino.sql.tree.OrderBy;
import io.trino.sql.tree.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class BuiltinFunctionCallBuilder
{
    private final Metadata metadata;
    private String name;
    private List<TypeSignature> argumentTypes = new ArrayList<>();
    private List<Expression> argumentValues = new ArrayList<>();
    private Optional<NodeLocation> location = Optional.empty();
    private Optional<Window> window = Optional.empty();
    private Optional<Expression> filter = Optional.empty();
    private Optional<OrderBy> orderBy = Optional.empty();
    private boolean distinct;

    public static BuiltinFunctionCallBuilder resolve(Metadata metadata)
    {
        return new BuiltinFunctionCallBuilder(metadata);
    }

    private BuiltinFunctionCallBuilder(Metadata metadata)
    {
        this.metadata = requireNonNull(metadata, "metadata is null");
    }

    public BuiltinFunctionCallBuilder setName(String name)
    {
        this.name = requireNonNull(name, "name is null");
        return this;
    }

    public BuiltinFunctionCallBuilder addArgument(Type type, Expression value)
    {
        requireNonNull(type, "type is null");
        return addArgument(type.getTypeSignature(), value);
    }

    public BuiltinFunctionCallBuilder addArgument(TypeSignature typeSignature, Expression value)
    {
        requireNonNull(typeSignature, "typeSignature is null");
        requireNonNull(value, "value is null");
        argumentTypes.add(typeSignature);
        argumentValues.add(value);
        return this;
    }

    public BuiltinFunctionCallBuilder setArguments(List<Type> types, List<Expression> values)
    {
        requireNonNull(types, "types is null");
        requireNonNull(values, "values is null");
        argumentTypes = types.stream()
                .map(Type::getTypeSignature)
                .collect(Collectors.toList());
        argumentValues = new ArrayList<>(values);
        return this;
    }

    public BuiltinFunctionCallBuilder setLocation(NodeLocation location)
    {
        this.location = Optional.of(requireNonNull(location, "location is null"));
        return this;
    }

    public BuiltinFunctionCallBuilder setWindow(Window window)
    {
        this.window = Optional.of(requireNonNull(window, "window is null"));
        return this;
    }

    public BuiltinFunctionCallBuilder setWindow(Optional<Window> window)
    {
        this.window = requireNonNull(window, "window is null");
        return this;
    }

    public BuiltinFunctionCallBuilder setFilter(Expression filter)
    {
        this.filter = Optional.of(requireNonNull(filter, "filter is null"));
        return this;
    }

    public BuiltinFunctionCallBuilder setFilter(Optional<Expression> filter)
    {
        this.filter = requireNonNull(filter, "filter is null");
        return this;
    }

    public BuiltinFunctionCallBuilder setOrderBy(OrderBy orderBy)
    {
        this.orderBy = Optional.of(requireNonNull(orderBy, "orderBy is null"));
        return this;
    }

    public BuiltinFunctionCallBuilder setOrderBy(Optional<OrderBy> orderBy)
    {
        this.orderBy = requireNonNull(orderBy, "orderBy is null");
        return this;
    }

    public BuiltinFunctionCallBuilder setDistinct(boolean distinct)
    {
        this.distinct = distinct;
        return this;
    }

    public FunctionCall build()
    {
        ResolvedFunction resolvedFunction = metadata.resolveBuiltinFunction(name, TypeSignatureProvider.fromTypeSignatures(argumentTypes));
        return new FunctionCall(
                location,
                resolvedFunction.toQualifiedName(),
                window,
                filter,
                orderBy,
                distinct,
                Optional.empty(),
                Optional.empty(),
                argumentValues);
    }
}

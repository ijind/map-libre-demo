/*
 * Copyright © zhaoyuntao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.txai.common.permission.checker;

import android.content.Context;

import java.util.List;

public final class DoubleChecker implements PermissionChecker {

    private static final PermissionChecker STANDARD_CHECKER = new StandardChecker();

    @Override
    public boolean hasPermission(Context context, String... permissions) {
        return STANDARD_CHECKER.hasPermission(context, permissions);
    }

    @Override
    public boolean hasPermission(Context context, List<String> permissions) {
        return STANDARD_CHECKER.hasPermission(context, permissions);
    }
}
// SonarQube IaC Plugin
// Copyright (C) 2021-2023 SonarSource SA
// mailto:info AT sonarsource DOT com
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this program; if not, write to the Free Software Foundation,
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

package main

import (
	"errors"
	"github.com/Masterminds/sprig/v3"
	"slices"
	"strconv"
	"text/template"
)

var sprigFunctionsWhitelist = []string{
	"abbrev",
	"abbrevboth",
	"add",
	"add1",
	"add1f",
	"addf",
	"adler32sum",
	"ago",
	"append",
	"atoi",
	"b32dec",
	"b32enc",
	"b64dec",
	"b64enc",
	"base",
	"buildCustomCert",
	"camelcase",
	"cat",
	"ceil",
	"clean",
	"coalesce",
	"compact",
	"concat",
	"contains",
	"date",
	"dateInZone",
	"dateModify",
	"decryptAES",
	"deepCopy",
	"deepEqual",
	"default",
	"derivePassword",
	"dict",
	"dig",
	"dir",
	"div",
	"divf",
	"duration",
	"durationRound",
	"empty",
	"encryptAES",
	"ext",
	"fail",
	"first",
	"float64",
	"floor",
	"genCA",
	"genPrivateKey",
	"genSelfSignedCert",
	"genSignedCert",
	"get",
	"has",
	"hasKey",
	"hasPrefix",
	"hasSuffix",
	"htmlDate",
	"htmlDateInZone",
	"htpasswd",
	"indent",
	"initial",
	"initials",
	"int",
	"int64",
	"isAbs",
	"kebabcase",
	"keys",
	"kindIs",
	"kindOf",
	"last",
	"list",
	"lower",
	"max",
	"maxf",
	"merge",
	"mergeOverwrite",
	"min",
	"minf",
	"mod",
	"mul",
	"mulf",
	"mustAppend",
	"mustCompact",
	"mustDateModify",
	"mustDeepCopy",
	"mustFirst",
	"mustHas",
	"mustInitial",
	"mustLast",
	"mustMerge",
	"mustMergeOverwrite",
	"mustPrepend",
	"mustRegexFind",
	"mustRegexFindAll",
	"mustRegexMatch",
	"mustRegexReplaceAll",
	"mustRegexReplaceAllLiteral",
	"mustRest",
	"mustReverse",
	"mustSlice",
	"mustToDate",
	"mustToJson",
	"mustToPrettyJson",
	"mustToRawJson",
	"mustUniq",
	"mustWithout",
	"nindent",
	"nospace",
	"now",
	"omit",
	"pick",
	"pluck",
	"plural",
	"prepend",
	"quote",
	"randAlpha",
	"randAlphaNum",
	"randAscii",
	"randNumeric",
	"regexFind",
	"regexFindAll",
	"regexMatch",
	"regexReplaceAll",
	"regexReplaceAllLiteral",
	"regexSplit",
	"regexSplit",
	"repeat",
	"replace",
	"rest",
	"reverse",
	"round",
	"semver",
	"semverCompare",
	"seq",
	"set",
	"sha1sum",
	"sha256sum",
	"shuffle",
	"slice",
	"snakecase",
	"squote",
	"sub",
	"subf",
	"substr",
	"swapcase",
	"ternary",
	"title",
	"toDate",
	"toDecimal",
	"toJson",
	"toPrettyJson",
	"toRawJson",
	"toString",
	"toStrings",
	"trim",
	"trimAll",
	"trimPrefix",
	"trimSuffix",
	"trunc",
	"typeIs",
	"typeIsLike",
	"typeOf",
	"uniq",
	"unixEpoch",
	"unset",
	"until",
	"untilStep",
	"untitle",
	"upper",
	"urlJoin",
	"urlParse",
	"uuidv4",
	"values",
	"without",
	"wrap",
	"wrapWith",
}

var sprigFunctions = initSprigFunctions()

var generatedNamesCount = 0

func initSprigFunctions() template.FuncMap {
	sprigAllFunctions := sprig.TxtFuncMap()

	result := make(template.FuncMap)
	for key, value := range sprigAllFunctions {
		if slices.Contains(sprigFunctionsWhitelist, key) {
			result[key] = value
		}
	}
	return result
}

type Values = struct{ Values map[string]interface{} }

func addCustomFunctions() *template.FuncMap {
	functions := sprigFunctions

	functions["lookup"] = func(string, string, string, string) (map[string]interface{}, error) {
		return map[string]interface{}{}, nil
	}

	functions["tpl"] = func(templateContent string, values Values) (string, error) {
		// TODO SONARIAC-1177 Add "tpl" function to Helm template evaluation
		text := "sonar-generated-tpl-" + strconv.Itoa(generatedNamesCount)
		generatedNamesCount++
		return text, nil
	}

	functions["include"] = func(name string, data interface{}) (string, error) {
		// TODO SONARIAC-1176 Add "include" function to Helm template evaluation
		text := "sonar-generated-include-" + strconv.Itoa(generatedNamesCount)
		generatedNamesCount++
		return text, nil
	}

	functions["required"] = required

	functions["getHostByName"] = func(name string) string {
		// IP for documentation purpose
		return "192.0.2.0"
	}
	functions["fail"] = emptyText

	return &functions
}

func required(warningMessage string, value interface{}) (interface{}, error) {
	if value == nil {
		return nil, errors.New(warningMessage)
	}
	text, ok := value.(string)
	if ok && text == "" {
		return text, errors.New(warningMessage)
	}
	return value, nil
}

func emptyText(_ string) string {
	return ""
}

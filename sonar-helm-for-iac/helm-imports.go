// SonarQube IaC Plugin
// Copyright (C) 2018-2023 SonarSource SA
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
	"github.com/Masterminds/sprig/v3"
	"slices"
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

func addCustomFunctions() *template.FuncMap {
	funcMap := sprigFunctions

	funcMap["lookup"] = func(string, string, string, string) (map[string]interface{}, error) {
		return map[string]interface{}{}, nil
	}

	funcMap["getHostByName"] = func(name string) string {
		return ""
	}

	return &funcMap
}
